package student.benchmark.analysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BenchmarkAnalyzer} covering statistics not exercised by
 * {@link BenchmarkAnalyzerTest}: percentiles, standard deviation, coefficient
 * of variation, runtime mean, and output sort order.
 */
class BenchmarkAnalyzerStatisticsTest {

    private static BenchmarkRun run(String alg, int moves, boolean success) {
        return new BenchmarkRun(alg, 0L, success, moves, 100L);
    }

    private static BenchmarkRun runWithRuntime(String alg, int moves, long runtimeUs) {
        return new BenchmarkRun(alg, 0L, true, moves, runtimeUs);
    }

    // --- Percentile ---

    @Test
    void p90IsCorrectForTenElementList() {
        // Sorted: [10,20,30,40,50,60,70,80,90,100]
        // p90 index = 0.90 * 9 = 8.1 → interpolation: 90*(0.9) + 100*(0.1) = 91
        List<BenchmarkRun> runs = List.of(
                run("A", 10, true), run("A", 20, true), run("A", 30, true),
                run("A", 40, true), run("A", 50, true), run("A", 60, true),
                run("A", 70, true), run("A", 80, true), run("A", 90, true),
                run("A", 100, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(91.0, stats.p90Moves(), 0.001);
    }

    @Test
    void medianIsMidpointForEvenSizedList() {
        // Sorted: [10, 20, 30, 40] → median index = 0.5 * 3 = 1.5
        // → 20 * 0.5 + 30 * 0.5 = 25
        List<BenchmarkRun> runs = List.of(
                run("A", 10, true), run("A", 20, true),
                run("A", 30, true), run("A", 40, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(25.0, stats.medianMoves(), 0.001);
    }

    @Test
    void p95ForSingleRunIsTheRunValue() {
        List<BenchmarkRun> runs = List.of(run("A", 50, true));

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(50.0, stats.p95Moves(), 0.001);
    }

    // --- Standard deviation and CV ---

    @Test
    void stdIsZeroForSingleRun() {
        List<BenchmarkRun> runs = List.of(run("A", 42, true));

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(0.0, stats.stdMoves(), 0.001);
    }

    @Test
    void coefficientOfVariationIsStdOverMean() {
        // moves: [10, 20, 30], mean=20
        // sample std = sqrt(((10-20)^2 + (20-20)^2 + (30-20)^2) / 2)
        //            = sqrt(200/2) = sqrt(100) = 10
        // CV = 10 / 20 = 0.5
        List<BenchmarkRun> runs = List.of(
                run("A", 10, true),
                run("A", 20, true),
                run("A", 30, true)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(0.5, stats.coefficientOfVariation(), 0.001);
    }

    @Test
    void coefficientOfVariationIsZeroWhenMeanIsZero() {
        // No successful runs → mean=0, std=0; CV must be 0, not NaN.
        List<BenchmarkRun> runs = List.of(
                run("A", 10, false),
                run("A", 20, false)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(0.0, stats.coefficientOfVariation(), 0.001);
    }

    // --- Runtime ---

    @Test
    void meanRuntimeExcludesFailedRuns() {
        List<BenchmarkRun> runs = List.of(
                new BenchmarkRun("A", 0L, true,  10, 500L),
                new BenchmarkRun("A", 0L, false, 99, 9999L)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(500.0, stats.meanRuntimeUs(), 0.001);
    }

    @Test
    void meanRuntimeIsAverageAcrossSuccessfulRuns() {
        List<BenchmarkRun> runs = List.of(
                runWithRuntime("A", 10, 100L),
                runWithRuntime("A", 20, 300L)
        );

        BenchmarkStatistics stats = new BenchmarkAnalyzer().analyze(runs).get(0);

        assertEquals(200.0, stats.meanRuntimeUs(), 0.001);
    }

    // --- Sort order ---

    @Test
    void outputIsSortedByMeanMovesAscending() {
        List<BenchmarkRun> runs = List.of(
                run("Slow",   100, true),
                run("Fast",    10, true),
                run("Medium",  50, true)
        );

        List<BenchmarkStatistics> stats = new BenchmarkAnalyzer().analyze(runs);

        assertEquals("Fast",   stats.get(0).algorithm());
        assertEquals("Medium", stats.get(1).algorithm());
        assertEquals("Slow",   stats.get(2).algorithm());
    }
}