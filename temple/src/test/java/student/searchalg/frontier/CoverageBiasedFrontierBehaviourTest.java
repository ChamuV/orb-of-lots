package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end behaviour tests for {@link CoverageBiasedFrontierUtilitySearch}
 * on graphs where the coverage density term actually influences frontier
 * selection.
 *
 * <p>Tests cover: linear graphs (density always zero), star graphs (non-zero
 * simultaneous frontier density), deep branching, disconnected graphs,
 * move-count correctness, and the boundary behaviour at mu=0.
 */
class CoverageBiasedFrontierBehaviourTest {

    /**
     * mu=0 must be accepted without throwing and must still find the orb.
     */
    @Test
    void muZeroStillFindsOrb() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(2)
                .edge(0, 1).edge(1, 2)
                .distance(0, 2).distance(1, 1).distance(2, 0)
                .build();

        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(0.0, r -> {});

        assertDoesNotThrow(() -> alg.findOrb(state));
        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Linear chain: only one frontier node exists at a time, so density is
     * always zero and the algorithm reduces to the base utility score.
     * A high mu must not prevent the orb from being found.
     */
    @Test
    void findsOrbOnLinearGraphWhereDensityIsAlwaysZero() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(5.0, r -> {}).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Star graph: after visiting node 0, nodes 1, 2 and 3 all enter the
     * frontier simultaneously. Each has one discovered neighbour (node 0),
     * giving a non-zero density. A high mu should still produce a valid run.
     */
    @Test
    void findsOrbOnStarGraphWithHighMu() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(0, 2).edge(0, 3)
                .distance(0, 1).distance(1, 4).distance(2, 4).distance(3, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(10.0, r -> captured[0] = r)
                .findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].isSuccess());
    }

    /**
     * Star graph with mu=0: the algorithm should select the frontier node
     * with the smallest orb distance (node 3), reaching the orb in 1 move.
     * This confirms the base utility score drives selection when coverage is
     * disabled.
     */
    @Test
    void muZeroOnStarSelectsClosestFrontierNode() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(0, 2).edge(0, 3)
                .distance(0, 1).distance(1, 4).distance(2, 4).distance(3, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(0.0, r -> captured[0] = r)
                .findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertEquals(1, captured[0].getMoves());
    }

    /**
     * Grid-like graph where some frontier nodes are adjacent to several already
     * discovered nodes (high density) and others are not. Verifies that the
     * algorithm terminates and finds the orb regardless of which candidates are
     * selected.
     *
     * <pre>
     *   0 -- 1 -- 2
     *   |         |
     *   3 -- 4 -- 5(orb)
     * </pre>
     */
    @Test
    void findsOrbOnGridGraphWithVaryingDensity() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(5)
                .edge(0, 1).edge(1, 2).edge(2, 5)
                .edge(0, 3).edge(3, 4).edge(4, 5)
                .distance(0, 3).distance(1, 3).distance(2, 2)
                .distance(3, 2).distance(4, 1).distance(5, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(2.0, r -> {}).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Deep linear chain with a dead-end branch near the start.
     * The coverage term must not cause the algorithm to prefer the dead-end
     * branch indefinitely.
     *
     * <pre>
     *   0 -- 1 -- 2 -- 3 -- 4(orb)
     *        |
     *        5 (dead end)
     * </pre>
     */
    @Test
    void findsOrbPastDeadEndWithHighMu() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 2).edge(2, 3).edge(3, 4)
                .edge(1, 5)
                .distance(0, 4).distance(1, 3).distance(2, 2).distance(3, 1)
                .distance(4, 0).distance(5, 5)
                .build();

        new CoverageBiasedFrontierUtilitySearch(5.0, r -> {}).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Two long branches from the root; orb is at the end of one.
     * The algorithm must eventually explore both branches and find the orb.
     */
    @Test
    void findsOrbOnTwoBranchGraph() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(5)
                .edge(0, 1).edge(1, 2).edge(2, 3)   // dead branch
                .edge(0, 4).edge(4, 5)               // orb branch
                .distance(0, 2).distance(1, 4).distance(2, 5).distance(3, 6)
                .distance(4, 1).distance(5, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(1.0, r -> {}).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Disconnected graph — orb component is unreachable.
     * The frontier empties without finding the orb; must terminate without
     * throwing and must not report the orb as found.
     */
    @Test
    void terminatesGracefullyOnDisconnectedGraph() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(99)
                .edge(0, 1).edge(1, 2)
                .distance(0, 5).distance(1, 4).distance(2, 3)
                .distance(99, 0)
                .build();

        assertDoesNotThrow(() ->
                new CoverageBiasedFrontierUtilitySearch(2.0, r -> {}).findOrb(state));

        assertNotEquals(0, state.getDistanceToTarget(),
                "Orb should not have been reached on a disconnected graph");
    }

    /**
     * Move count must be positive and success true after a successful run.
     */
    @Test
    void recordsMovesAndSuccessCorrectly() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        new CoverageBiasedFrontierUtilitySearch(1.0, r -> captured[0] = r)
                .findOrb(state);

        assertTrue(captured[0].getMoves() > 0);
        assertTrue(captured[0].isSuccess());
    }
}