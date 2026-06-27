package student.searchalg.bfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BreadthFirstSearch}.
 *
 * <p>Verifies that BFS finds the Orb, records moves correctly, and
 * reports success. The separation between physical visitation and BFS
 * expansion is verified implicitly — incorrect conflation would cause
 * the algorithm to return without finding the Orb.
 */
class BreadthFirstSearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        BreadthFirstSearch alg = new BreadthFirstSearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        BreadthFirstSearch alg = new BreadthFirstSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        BreadthFirstSearch alg = new BreadthFirstSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}