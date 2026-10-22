package com.example.jitterbackoff.web.dto;

import java.util.List;
import java.util.UUID;

public record ComparisonResponse(
        UUID runId,
        SimulationConfigDto config,
        List<StrategyResultDto> results,
        LoadSignal loadSignal    // server-side contention summary across all strategies
) {}
