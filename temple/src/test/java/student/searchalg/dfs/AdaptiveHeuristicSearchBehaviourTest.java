package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour tests for {@link AdaptiveHeuristicSearch} on graphs where
 * the heuristic update logic actually fires (requires dead ends and backtracking).
 */
class AdaptiveHeuristicSearchBehaviourTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = r -> {};

    /**
     * Dead-end branch:
     * <pre>
     *   0 -- 1 -- 2(dead end)
     *   |
     *   3(orb)
     * </pre>
     * After exploring 1->2 and backtracking, the heuristic for node 1
     * should be raised. The algorithm must still reach node 3.
     */
    @Test
    void findsOrbAfterExploringDeadEndBranch() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2)
                .edge(0, 3)
                .distance(0, 2).distance(1, 3).distance(2, 4).distance(3, 0)
                .build();

        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(NO_OP);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Linear chain — heuristic updates are monotone so the algorithm
     * should never get stuck in a cycle.
     */
    @Test
    void doesNotLoopOnLinearGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(r -> captured[0] = r);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].isSuccess());
    }

    /**
     * Misleading branch: the closer hint leads to a dead end; the orb is on
     * the other side. The algorithm must backtrack and find it.
     */
    @Test
    void backtracksThroughMisleadingBranchToFindOrb() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 2)
                .edge(0, 3).edge(3, 4)
                .distance(0, 3)
                .distance(1, 1).distance(2, 2)
                .distance(3, 2).distance(4, 0)
                .build();

        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(NO_OP);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }
}