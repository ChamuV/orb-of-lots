package student.benchmark.analysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BenchmarkComparisonAnalyzer}.
 *
 * <p>Verifies that win counts are correctly assigned to the algorithm
 * with the fewest moves per seed, and that failed runs are excluded.
 */
class BenchmarkComparisonAnalyzerTest {

    private static BenchmarkRun run(String alg, long seed, int moves, boolean success) {
        return new BenchmarkRun(alg, seed, success, moves, 0L);
    }

    @Test
    void assignsWinToAlgorithmWithFewestMoves() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 30, true),
                run("AlgB", 1L, 20, true)
        );

        List<AlgorithmWinCount> wins =
                new BenchmarkComparisonAnalyzer().computeWinCounts(runs);

        assertEquals("AlgB", wins.get(0).algorithm());
        assertEquals(1, wins.get(0).wins());
    }

    @Test
    void excludesFailedRunsFromWinCounts() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 10, false),
                run("AlgB", 1L, 20, true)
        );

        List<AlgorithmWinCount> wins =
                new BenchmarkComparisonAnalyzer().computeWinCounts(runs);

        assertEquals("AlgB", wins.get(0).algorithm());
    }

    @Test
    void countsWinsAcrossMultipleSeeds() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 1L, 10, true),
                run("AlgB", 1L, 20, true),
                run("AlgA", 2L, 30, true),
                run("AlgB", 2L, 15, true)
        );
    
        List<AlgorithmWinCount> wins =
                new BenchmarkComparisonAnalyzer().computeWinCounts(runs);
    
        assertEquals(2, wins.size());
    
        long algAWins = wins.stream()
                .filter(w -> w.algorithm().equals("AlgA"))
                .mapToLong(AlgorithmWinCount::wins)
                .sum();
    
        long algBWins = wins.stream()
                .filter(w -> w.algorithm().equals("AlgB"))
                .mapToLong(AlgorithmWinCount::wins)
                .sum();
    
        assertEquals(1, algAWins);
        assertEquals(1, algBWins);
    }
}