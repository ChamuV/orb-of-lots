package student.searchalg.rta;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RealTimeAStarSearch}.
 *
 * <p>Verifies that the algorithm finds the Orb, records moves correctly,
 * and reports success. Heuristic update behaviour is verified implicitly
 * through successful completion on a simple graph.
 */
class RealTimeAStarSearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        RealTimeAStarSearch alg = new RealTimeAStarSearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        RealTimeAStarSearch alg = new RealTimeAStarSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        RealTimeAStarSearch alg = new RealTimeAStarSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}