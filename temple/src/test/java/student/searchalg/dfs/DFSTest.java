package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DFS}.
 *
 * <p>Verifies that DFS finds the Orb, records the correct move count,
 * and reports success on a simple two-node graph.
 */
class DFSTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void findsOrbOnTwoNodeGraph() {
        DFS dfs = new DFS(NO_OP);
        StubExplorationState state = new StubExplorationState();

        dfs.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        DFS dfs = new DFS(result -> captured[0] = result);

        dfs.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        DFS dfs = new DFS(result -> captured[0] = result);

        dfs.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}