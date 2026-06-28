package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.GraphExplorationState;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GradientFrontierUtilitySearch}.
 *
 * <p>Covers parameter validation, exact score arithmetic across all
 * gradient cases (positive, zero and negative gain), lambda amplification,
 * and end-to-end behaviour on linear, branching and disconnected graphs.
 */
class GradientFrontierUtilitySearchTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void zeroLambdaThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new GradientFrontierUtilitySearch(0.0, NO_OP));
    }

    @Test
    void negativeLambdaThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new GradientFrontierUtilitySearch(-1.0, NO_OP));
    }

    /**
     * score = travelCost - lambda * (h(current) - h(candidate))
     *       = 3 - 1.0 * (10 - 5) = 3 - 5 = -2
     */
    @Test
    void positiveGainReducesScoreByExactAmount() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(1.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(0L, 10, 1L, 5);

        assertEquals(-2.0, alg.score(1L, 0L, travelCost, distToOrb), 0.001);
    }

    /**
     * Zero gradient gain: candidate is at the same distance as current.
     * score = travelCost - lambda * 0 = travelCost
     */
    @Test
    void zeroGainScoreEqualsTravelCost() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(2.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 4);
        Map<Long, Integer> distToOrb  = Map.of(0L, 7, 1L, 7);

        assertEquals(4.0, alg.score(1L, 0L, travelCost, distToOrb), 0.001);
    }

    /**
     * Negative gain: candidate is further from the orb than current.
     * score = travelCost - lambda * (negative) = travelCost + penalty
     * = 3 - 1.0 * (5 - 10) = 3 + 5 = 8
     */
    @Test
    void negativeGainIncreasesScore() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(1.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(0L, 5, 1L, 10);

        assertEquals(8.0, alg.score(1L, 0L, travelCost, distToOrb), 0.001);
    }

    /**
     * Lambda scales the gradient term linearly.
     * lambda=2: 3 - 2.0 * 5 = -7
     */
    @Test
    void lambdaScalesGradientTermLinearly() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(2.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(0L, 10, 1L, 5);

        assertEquals(-7.0, alg.score(1L, 0L, travelCost, distToOrb), 0.001);
    }

    @Test
    void higherLambdaProducesLowerScoreForPositiveGain() {
        GradientFrontierUtilitySearch lowLambda =
                new GradientFrontierUtilitySearch(1.0, NO_OP);
        GradientFrontierUtilitySearch highLambda =
                new GradientFrontierUtilitySearch(5.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(0L, 10, 1L, 5);

        assertTrue(highLambda.score(1L, 0L, travelCost, distToOrb)
                 < lowLambda.score(1L, 0L, travelCost, distToOrb));
    }

    /**
     * With equal travel costs, the candidate closest to the orb (steepest
     * descent) should receive the lowest score and therefore be preferred.
     */
    @Test
    void prefersSteeperDescentWhenTravelCostsAreEqual() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(1.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 2, 2L, 2);
        Map<Long, Integer> distToOrb  = Map.of(0L, 10, 1L, 3, 2L, 7);

        double scoreA = alg.score(1L, 0L, travelCost, distToOrb);
        double scoreB = alg.score(2L, 0L, travelCost, distToOrb);

        assertTrue(scoreA < scoreB,
                "Candidate with greater descent should have lower score");
    }

    /**
     * Linear chain: 0 -- 1 -- 2 -- 3(orb).
     * Every step is a descent toward the orb; the algorithm must navigate
     * all three hops and terminate correctly.
     */
    @Test
    void findsOrbOnLinearChain() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        new GradientFrontierUtilitySearch(1.0, NO_OP).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Branching graph:
     * <pre>
     *   0 -- 1(dist=5, dead end)
     *   |
     *   2 -- 3(orb, dist=0)
     * </pre>
     * Node 2 offers a larger descent than node 1; the algorithm should prefer
     * it and reach the orb efficiently.
     */
    @Test
    void prefersSteepestDescentBranchOnBranchingGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1)
                .edge(0, 2).edge(2, 3)
                .distance(0, 2).distance(1, 5).distance(2, 1).distance(3, 0)
                .build();

        new GradientFrontierUtilitySearch(1.0, r -> captured[0] = r).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].isSuccess());
    }

    /**
     * Two equally deep branches; the orb is on the branch with the steeper
     * gradient. With a sufficiently large lambda, that branch should be chosen.
     *
     * <pre>
     *   0 -- 1(dist=8) -- 2(dead end, dist=9)
     *   |
     *   3(dist=3) -- 4(orb, dist=0)
     * </pre>
     */
    @Test
    void findsOrbOnDeepBranchingGraphWithHighLambda() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 2)
                .edge(0, 3).edge(3, 4)
                .distance(0, 4).distance(1, 8).distance(2, 9)
                .distance(3, 3).distance(4, 0)
                .build();

        new GradientFrontierUtilitySearch(3.0, NO_OP).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Orb is on the branch with the shallower gradient; the algorithm must
     * still find it after exploring the steeper (but incorrect) branch.
     */
    @Test
    void evenuallyFindsOrbOnShallowestBranch() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(2)
                .edge(0, 1).edge(0, 2)
                .distance(0, 2).distance(1, 0).distance(2, 0)
                .build();

        // node 1 has the same descent as node 2 but orb is at node 2 with dist=0;
        // the algorithm must explore both.
        new GradientFrontierUtilitySearch(1.0, NO_OP).findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Disconnected graph — orb component is unreachable.
     * The frontier must empty without throwing.
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
                new GradientFrontierUtilitySearch(1.0, NO_OP).findOrb(state));
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

        new GradientFrontierUtilitySearch(1.0, r -> captured[0] = r).findOrb(state);

        assertTrue(captured[0].getMoves() > 0);
        assertTrue(captured[0].isSuccess());
    }
}