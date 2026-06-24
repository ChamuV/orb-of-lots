package student.benchmark.analysis;

/**
 * Number of benchmark seeds won by an algorithm.
 */
public record AlgorithmWinCount(
        String algorithm,
        long wins
) {
}