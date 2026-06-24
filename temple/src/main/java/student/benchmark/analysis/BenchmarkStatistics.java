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
        int bestMoves,
        int worstMoves,
        double meanRuntimeMs
) {
}