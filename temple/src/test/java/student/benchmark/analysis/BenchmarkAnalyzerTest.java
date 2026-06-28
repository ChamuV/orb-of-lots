package student.benchmark.analysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BenchmarkAnalyzer}.
 *
 * <p>Verifies mean calculation, success rate filtering, and correct
 * identification of best and worst move counts from benchmark runs.
 */
class BenchmarkAnalyzerTest {

    private static BenchmarkRun run(String alg, int moves, boolean success) {
        return new BenchmarkRun(alg, 0L, success, moves, 0L);
    }

    @Test
    void computesCorrectMeanMoves() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 10, true),
                run("AlgA", 20, true),
                run("AlgA", 30, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(20.0, stats.meanMoves(), 0.001);
    }

    @Test
    void excludesFailedRunsFromStatistics() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 10, true),
                run("AlgA", 100, false),
                run("AlgA", 20, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(2, stats.successes());
        assertEquals(1, stats.failures());
        assertEquals(15.0, stats.meanMoves(), 0.001);
    }

    @Test
    void identifiesBestAndWorstMoves() {
        List<BenchmarkRun> runs = List.of(
                run("AlgA", 50, true),
                run("AlgA", 10, true),
                run("AlgA", 30, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(10, stats.bestMoves());
        assertEquals(50, stats.worstMoves());
    }
}