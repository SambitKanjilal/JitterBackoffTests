package com.example.jitterbackoff.web.dto;

/**
 * Server-side contention summary embedded in /results responses.
 *
 * Callers use these values to shrink retry budgets under pressure:
 *   if (contentionRate > 0.7) nextConfig.maxAttempts = signal.recommendedMaxAttempts;
 *
 * This is also emitted as response headers (X-Contention-Rate, X-Recommended-*) so
 * HTTP intermediaries and SDKs can act on load without parsing the body.
 */
public record LoadSignal(
        double contentionRate,         // wastedCalls / totalCalls across all strategies
        String bestStrategy,           // strategy with fewest wastedCalls
        int recommendedMaxAttempts,    // suggested retry budget for next call under this load
        long recommendedCapMs,         // suggested cap for next call
        double bestStrategyGoodput     // goodput of the best-performing strategy
) {}
