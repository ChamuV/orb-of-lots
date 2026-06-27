package student.searchalg.random;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RandomWalkSearch}.
 *
 * <p>Verifies that the algorithm finds the Orb, records moves, and
 * reports success on a simple graph. Frontier recovery behaviour is
 * verified implicitly — on the two-node stub graph no recovery is
 * needed, so this tests the happy path only.
 */
class RandomWalkSearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        RandomWalkSearch alg = new RandomWalkSearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        RandomWalkSearch alg = new RandomWalkSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        RandomWalkSearch alg = new RandomWalkSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}