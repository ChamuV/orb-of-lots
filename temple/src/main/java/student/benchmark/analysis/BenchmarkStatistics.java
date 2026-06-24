package student.benchmark.analysis;

/**
 * Aggregate statistics for a benchmarked search algorithm.
 */
public record BenchmarkStatistics(
        String algorithm,
        int runs,
        int successes,
        int failures,
        double successRate,
        double meanMoves,
        double medianMoves,
        double stdMoves,
        double coefficientOfVariation,
        double p90Moves,
        double p95Moves,
        int bestMoves,
        int worstMoves,
        double meanRuntimeUs
) {
}