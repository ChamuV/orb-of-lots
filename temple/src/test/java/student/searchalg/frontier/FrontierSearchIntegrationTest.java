package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for frontier search algorithms verifying the full
 * {@link student.searchalg.Algorithm} template-method pipeline.
 *
 * <p>Confirms that move counting, success detection, and benchmark
 * writer invocation work correctly for both {@link FrontierUtilitySearch}
 * and {@link CoverageBiasedFrontierUtilitySearch}.
 */
class FrontierSearchIntegrationTest {

    @Test
    void frontierWriterIsInvokedAfterSearch() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        FrontierUtilitySearch alg = new FrontierUtilitySearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertNotNull(captured[0]);
    }

    @Test
    void frontierMoveCountAndSuccessAreRecordedTogether() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        FrontierUtilitySearch alg = new FrontierUtilitySearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
        assertTrue(captured[0].isSuccess());
    }

    @Test
    void coverageBiasedAlsoCompletesSuccessfully() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(1.0, r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
        assertEquals(1, captured[0].getMoves());
    }
}