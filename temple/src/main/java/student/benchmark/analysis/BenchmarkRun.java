package student.benchmark.analysis;

/**
 * Represents a single benchmark run loaded from a CSV file.
 */
public record BenchmarkRun(
        String algorithm,
        long seed,
        boolean success,
        int moves,
        long runtimeMs
) {
}