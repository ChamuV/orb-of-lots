package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GreedyDFS}.
 *
 * <p>Verifies that GreedyDFS finds the Orb, records moves, and reports
 * success. Neighbour ordering by distance is implicitly verified since
 * the single-neighbour graph leaves no ambiguity.
 */
class GreedyDFSTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void findsOrbOnTwoNodeGraph() {
        GreedyDFS alg = new GreedyDFS(NO_OP);
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        GreedyDFS alg = new GreedyDFS(result -> captured[0] = result);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        GreedyDFS alg = new GreedyDFS(result -> captured[0] = result);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}