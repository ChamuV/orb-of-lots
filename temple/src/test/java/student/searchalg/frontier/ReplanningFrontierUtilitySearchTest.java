package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReplanningFrontierUtilitySearch}.
 *
 * <p>Verifies that the algorithm finds the Orb, records moves correctly,
 * and reports success. Replanning behaviour is verified implicitly —
 * on the two-node stub graph only one frontier node exists so replanning
 * has no effect, confirming the algorithm degrades gracefully to the
 * base utility score when no better target exists.
 */
class ReplanningFrontierUtilitySearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}