package com.example.jitterbackoff.web;

import com.example.jitterbackoff.domain.RunStatus;
import com.example.jitterbackoff.domain.SimulationRunEntity;
import com.example.jitterbackoff.domain.StrategyResultEntity;
import com.example.jitterbackoff.service.SimulationService;
import com.example.jitterbackoff.simulation.SimulationConfig;
import com.example.jitterbackoff.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /** POST /api/simulations              → run all 4 strategies          */
    /** POST /api/simulations?strategy=X   → run a single named strategy   */
    @PostMapping
    public ResponseEntity<SimulationStatusResponse> startRun(
            @Valid @RequestBody SimulationRequest request,
            @RequestParam(required = false) String strategy) {

        SimulationConfig config = toConfig(request);
        UUID runId = (strategy != null)
                ? simulationService.startSingleStrategyRun(strategy, config)
                : simulationService.startRun(config);

        return ResponseEntity
                .created(URI.create("/api/simulations/" + runId + "/status"))
                .body(toStatusResponse(simulationService.getRunStatus(runId)));
    }

    /** GET /api/simulations/{runId}/status */
    @GetMapping("/{runId}/status")
    public SimulationStatusResponse getStatus(@PathVariable UUID runId) {
        return toStatusResponse(simulationService.getRunStatus(runId));
    }

    /**
     * GET /api/simulations/{runId}/results
     *
     * Returns a comparison table with:
     *   - per-strategy goodput and wasted-call reduction vs NO_JITTER baseline
     *   - a LoadSignal body field with overall contention metrics
     *
     * Response headers (load-signaling layer):
     *   X-Contention-Rate          — fraction of all calls that were wasted (0.0–1.0)
     *   X-Best-Strategy            — strategy name with fewest wasted calls
     *   X-Recommended-Max-Attempts — suggested retry budget for the caller's next request
     *   X-Recommended-Cap-Ms       — suggested backoff cap for the caller's next request
     *   X-Best-Goodput             — goodput of the best-performing strategy
     *
     * These headers let HTTP intermediaries and SDKs shrink retry budgets without
     * parsing the body, cutting recovery time under sustained downstream pressure.
     */
    @GetMapping("/{runId}/results")
    public ResponseEntity<ComparisonResponse> getResults(@PathVariable UUID runId) {
        SimulationRunEntity run = simulationService.getRunStatus(runId);
        if (run.getStatus() == RunStatus.RUNNING) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<StrategyResultEntity> raw = simulationService.getResults(runId);
        LoadSignal signal = buildLoadSignal(raw, run.getNumClients());
        List<StrategyResultDto> dtos = toStrategyDtos(raw, run.getNumClients());
        ComparisonResponse body = new ComparisonResponse(run.getId(), toConfigDto(run), dtos, signal);

        HttpHeaders headers = loadSignalHeaders(signal);
        return ResponseEntity.ok().headers(headers).body(body);
    }

    /** GET /api/simulations — all runs, newest first */
    @GetMapping
    public List<SimulationStatusResponse> listRuns() {
        return simulationService.listAllRuns().stream().map(this::toStatusResponse).toList();
    }

    /** GET /api/simulations/compare — all completed runs with full results */
    @GetMapping("/compare")
    public List<ComparisonResponse> compareRuns() {
        return simulationService.listCompletedRuns().stream()
                .map(run -> {
                    List<StrategyResultEntity> raw = simulationService.getResults(run.getId());
                    LoadSignal signal = buildLoadSignal(raw, run.getNumClients());
                    return new ComparisonResponse(run.getId(), toConfigDto(run),
                            toStrategyDtos(raw, run.getNumClients()), signal);
                })
                .toList();
    }

    // --- exception handlers ---

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // --- load signal ---

    private static LoadSignal buildLoadSignal(List<StrategyResultEntity> results, int numClients) {
        if (results.isEmpty()) {
            return new LoadSignal(0.0, "N/A", 10, 400, 1.0);
        }
        long totalCalls  = results.stream().mapToLong(StrategyResultEntity::getTotalCalls).sum();
        long totalWasted = results.stream().mapToLong(StrategyResultEntity::getWastedCalls).sum();
        double contentionRate = totalCalls == 0 ? 0 : (double) totalWasted / totalCalls;

        StrategyResultEntity best = results.stream()
                .min(Comparator.comparingLong(StrategyResultEntity::getWastedCalls))
                .orElseThrow();

        // Recommend tighter retry budget proportional to contention; floor at 3
        int recommendedMax = Math.max(3, (int) Math.round(10 * (1 - contentionRate)));
        // Recommend a shorter cap when contention is high
        long recommendedCap = Math.round(400 * (1 - contentionRate * 0.5));

        double bestGoodput = (double) best.getSucceededClients() / numClients;
        return new LoadSignal(contentionRate, best.getStrategyName(),
                recommendedMax, recommendedCap, bestGoodput);
    }

    private static HttpHeaders loadSignalHeaders(LoadSignal signal) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Contention-Rate",          String.format("%.4f", signal.contentionRate()));
        h.set("X-Best-Strategy",             signal.bestStrategy());
        h.set("X-Recommended-Max-Attempts",  String.valueOf(signal.recommendedMaxAttempts()));
        h.set("X-Recommended-Cap-Ms",        String.valueOf(signal.recommendedCapMs()));
        h.set("X-Best-Goodput",              String.format("%.4f", signal.bestStrategyGoodput()));
        return h;
    }

    // --- mappers ---

    private static List<StrategyResultDto> toStrategyDtos(
            List<StrategyResultEntity> results, int numClients) {

        StrategyResultEntity noJitter = results.stream()
                .filter(r -> "NO_JITTER".equals(r.getStrategyName()))
                .findFirst().orElse(null);

        return results.stream().map(r -> {
            double goodput = (double) r.getSucceededClients() / numClients;
            Double wastedPct = null;
            Double wallPct   = null;
            if (noJitter != null && !"NO_JITTER".equals(r.getStrategyName())
                    && noJitter.getWastedCalls() > 0) {
                wastedPct = 100.0 * (r.getWastedCalls() - noJitter.getWastedCalls())
                        / noJitter.getWastedCalls();
                wallPct   = 100.0 * (r.getTotalDurationMs() - noJitter.getTotalDurationMs())
                        / noJitter.getTotalDurationMs();
            }
            return new StrategyResultDto(r.getStrategyName(), r.getTotalCalls(), r.getWastedCalls(),
                    r.getSucceededClients(), r.getTotalDurationMs(), r.getAvgAttemptsPerClient(),
                    goodput, wastedPct, wallPct);
        }).toList();
    }

    private SimulationStatusResponse toStatusResponse(SimulationRunEntity run) {
        String base = "/api/simulations/" + run.getId();
        return new SimulationStatusResponse(run.getId(), run.getStatus().name(),
                run.getStartedAt(), run.getCompletedAt(), toConfigDto(run),
                base + "/status", base + "/results");
    }

    private static SimulationConfigDto toConfigDto(SimulationRunEntity run) {
        return new SimulationConfigDto(run.getNumClients(), run.getBaseDelayMs(),
                run.getCapMs(), run.getMaxAttempts());
    }

    private static SimulationConfig toConfig(SimulationRequest req) {
        return new SimulationConfig(req.numClients(), req.baseDelayMs(), req.capMs(), req.maxAttempts());
    }
}
