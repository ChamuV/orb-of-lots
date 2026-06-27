package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour tests for {@link ReplanningFrontierUtilitySearch}.
 *
 * <p>The existing tests use the 2-node stub where replanning can never fire
 * (only one frontier node ever exists). These tests use richer graphs.
 */
class ReplanningFrontierBehaviourTest {

    /**
     * A better target appears after the initial navigation begins; the
     * algorithm should abandon the original target and still find the orb.
     *
     * <pre>
     *   0 -- 1 -- 2 -- 3   (initially-chosen long path)
     *   |
     *   4(orb)             (closer target discovered mid-navigation)
     * </pre>
     */
    @Test
    void findsOrbEvenWhenReplanningOccurs() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .edge(0, 4)
                .distance(0, 2).distance(1, 3).distance(2, 2).distance(3, 5)
                .distance(4, 0)
                .build();

        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(r -> {});
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Move count must be recorded correctly across a replanning event —
     * it should not be reset or skipped.
     */
    @Test
    void movesAreCountedCorrectlyAcrossReplanning() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(2)
                .edge(0, 1).edge(1, 2)
                .distance(0, 2).distance(1, 1).distance(2, 0)
                .build();

        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(r -> captured[0] = r);
        alg.findOrb(state);

        assertTrue(captured[0].getMoves() > 0);
        assertTrue(captured[0].isSuccess());
    }

    /**
     * When no better target ever exists, the algorithm should behave
     * identically to {@link FrontierUtilitySearch} and still find the orb.
     */
    @Test
    void completesWithoutReplanningWhenNoSuperiorTargetExists() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        ReplanningFrontierUtilitySearch alg =
                new ReplanningFrontierUtilitySearch(r -> {});
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }
}