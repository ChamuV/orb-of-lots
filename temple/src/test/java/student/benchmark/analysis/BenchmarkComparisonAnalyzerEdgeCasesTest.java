package student.benchmark.analysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for {@link BenchmarkComparisonAnalyzer}: ties, a single
 * algorithm, all-failed runs, and loser exclusion.
 */
class BenchmarkComparisonAnalyzerEdgeCasesTest {

    private static BenchmarkRun run(String alg, long seed, int moves, boolean success) {
        return new BenchmarkRun(alg, seed, success, moves, 0L);
    }

    /**
     * When two algorithms tie, exactly one of them should receive the win —
     * the total across both must be 1, not 0 or 2.
     */
    @Test
    void tieCountsAsExactlyOneWin() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 20, true),
                run("AlgB", 1L, 20, true)
        );

        long totalWins = new BenchmarkComparisonAnalyzer()
                .computeWinCounts(runs)
                .stream()
                .mapToLong(AlgorithmWinCount::wins)
                .sum();

        assertEquals(1, totalWins);
    }

    /**
     * A single algorithm with multiple seeds wins every seed.
     */
    @Test
    void singleAlgorithmWinsAllSeeds() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 10, true),
                run("AlgA", 2L, 20, true),
                run("AlgA", 3L, 30, true)
        );

        List<AlgorithmWinCount> wins =
                new BenchmarkComparisonAnalyzer().computeWinCounts(runs);

        assertEquals(1, wins.size());
        assertEquals(3, wins.get(0).wins());
    }

    /**
     * All runs failed — no wins for anyone; result should be empty.
     */
    @Test
    void returnsEmptyListWhenAllRunsFailed() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 10, false),
                run("AlgB", 1L, 5,  false)
        );

        assertTrue(new BenchmarkComparisonAnalyzer().computeWinCounts(runs).isEmpty());
    }

    /**
     * An algorithm that loses every seed must not appear in the output.
     */
    @Test
    void losingAlgorithmDoesNotAppearInWinList() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 5,  true),
                run("AlgB", 1L, 99, true)
        );

        boolean algBPresent = new BenchmarkComparisonAnalyzer()
                .computeWinCounts(runs)
                .stream()
                .anyMatch(w -> w.algorithm().equals("AlgB"));

        assertFalse(algBPresent);
    }
}