package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the backtracking logic in {@link BaseDFS}.
 *
 * <p>The 2-node stub used in {@link DFSTest} never exercises backtracking.
 * These tests use {@link GraphExplorationState} to verify the return-stack
 * correctly unwinds dead ends.
 */
class BaseDFSBacktrackingTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = r -> {};

    /**
     * Graph:
     * <pre>
     *   0 -- 1 -- 3(orb)
     *        |
     *        2  (dead end)
     * </pre>
     * DFS explores 1 -> 2 (dead end) -> backtracks to 1 -> goes to 3.
     */
    @Test
    void backtracksThroughDeadEnd() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(1, 3)
                .distance(0, 3).distance(1, 2).distance(2, 3).distance(3, 0)
                .build();

        DFS dfs = new DFS(NO_OP);
        dfs.findOrb(state);

        assertEquals(0, state.getDistanceToTarget(), "DFS should reach the orb");
    }

    /**
     * Linear chain: 0 -- 1 -- 2 -- 3(orb). No branching needed.
     */
    @Test
    void traversesLinearChainToOrb() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        DFS dfs = new DFS(NO_OP);
        dfs.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Two branches from node 0; the orb is on the second branch.
     *
     * <pre>
     *   0 -- 1 -- 2  (dead-end branch)
     *   |
     *   3 -- 4(orb)
     * </pre>
     */
    @Test
    void backtracksThroughEntireBranchBeforeTryingAlternative() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 2)
                .edge(0, 3).edge(3, 4)
                .distance(0, 4).distance(1, 3).distance(2, 4)
                .distance(3, 1).distance(4, 0)
                .build();

        DFS dfs = new DFS(NO_OP);
        dfs.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }
}