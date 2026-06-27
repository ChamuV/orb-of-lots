package student.searchalg.idastar;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link IterativeDeepeningAStarSearch}.
 *
 * <p>Verifies that IDA* finds the Orb, records the correct move count,
 * and reports success. The physical backtracking behaviour is verified
 * implicitly — on a two-node graph no backtracking occurs, so move
 * count directly reflects forward progress.
 */
class IterativeDeepeningAStarSearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        IterativeDeepeningAStarSearch alg =
                new IterativeDeepeningAStarSearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        IterativeDeepeningAStarSearch alg =
                new IterativeDeepeningAStarSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        IterativeDeepeningAStarSearch alg =
                new IterativeDeepeningAStarSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}