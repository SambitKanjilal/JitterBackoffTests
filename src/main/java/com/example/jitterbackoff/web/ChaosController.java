package com.example.jitterbackoff.web;

import com.example.jitterbackoff.chaos.ChaosState;
import com.example.jitterbackoff.web.dto.ChaosRequest;
import com.example.jitterbackoff.web.dto.ChaosStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controls the latency-spike injection used to demonstrate retry storms.
 *
 * Workflow to reproduce the "30s spike → retry storm → goodput at 86%" scenario:
 *
 *   1. POST /api/chaos  {"latencyMs": 30000}          # activate spike
 *   2. POST /api/simulations  {"numClients":20,...}   # run all 4 strategies under spike
 *   3. Poll GET /api/simulations/{id}/results         # observe degraded goodput + storms
 *   4. POST /api/chaos  {"latencyMs": 0}              # deactivate
 *   5. Run again without chaos to compare recovery
 *
 * Under a 30 s spike with maxAttempts=12 and a 20 ms base delay:
 *   - NO_JITTER clients cluster at identical retry times → thundering herd → goodput ~86%
 *   - Jittered strategies spread retries → better goodput, faster recovery after spike ends
 */
@RestController
@RequestMapping("/api/chaos")
public class ChaosController {

    private final ChaosState chaosState;

    public ChaosController(ChaosState chaosState) {
        this.chaosState = chaosState;
    }

    /** GET /api/chaos — current chaos state */
    @GetMapping
    public ChaosStatusResponse getStatus() {
        return toResponse();
    }

    /**
     * POST /api/chaos
     * Body: {"latencyMs": 30000}  → inject 30 s of latency per tryIncrement call
     * Body: {"latencyMs": 0}      → deactivate chaos
     */
    @PostMapping
    public ResponseEntity<ChaosStatusResponse> setChaos(@Valid @RequestBody ChaosRequest request) {
        if (request.latencyMs() == 0) {
            chaosState.deactivate();
        } else {
            chaosState.activate(request.latencyMs());
        }
        return ResponseEntity.ok(toResponse());
    }

    private ChaosStatusResponse toResponse() {
        if (chaosState.isActive()) {
            return new ChaosStatusResponse(true, chaosState.getSpikeMs(),
                    "Injecting " + chaosState.getSpikeMs() + " ms latency into every DB write attempt");
        }
        return new ChaosStatusResponse(false, 0, "No chaos active");
    }
}
