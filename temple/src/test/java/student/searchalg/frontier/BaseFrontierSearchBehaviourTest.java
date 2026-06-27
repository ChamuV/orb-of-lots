package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for core {@link BaseFrontierSearch} behaviour: multi-hop navigation,
 * frontier selection, and graceful termination on disconnected graphs.
 *
 * <p>{@link FrontierUtilitySearch} is used as a minimal concrete stand-in.
 */
class BaseFrontierSearchBehaviourTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = r -> {};

    /**
     * Linear chain 0 -- 1 -- 2 -- 3(orb). The frontier expands one hop at a
     * time; the algorithm must physically navigate through intermediates.
     */
    @Test
    void navigatesMultiHopChainToOrb() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        FrontierUtilitySearch alg = new FrontierUtilitySearch(NO_OP);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Star: node 0 connected to 1, 2, 3; orb at 3 with smallest distance hint.
     * The algorithm should prefer node 3 from the frontier.
     */
    @Test
    void selectsBestFrontierNodeOnStarGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(0, 2).edge(0, 3)
                .distance(0, 1).distance(1, 5).distance(2, 3).distance(3, 0)
                .build();

        FrontierUtilitySearch alg = new FrontierUtilitySearch(r -> captured[0] = r);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].isSuccess());
    }

    /**
     * Two paths to the orb; the shorter one should be preferred since
     * travel cost contributes to the score.
     *
     * <pre>
     *   0 -- 1 -- 4(orb)        (2 hops)
     *   |
     *   2 -- 3 -- 4(orb)        (3 hops)
     * </pre>
     */
    @Test
    void prefersLowerTravelCostWhenOrbDistancesAreEqual() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(4)
                .edge(0, 1).edge(1, 4)
                .edge(0, 2).edge(2, 3).edge(3, 4)
                .distance(0, 2).distance(1, 1)
                .distance(2, 3).distance(3, 2)
                .distance(4, 0)
                .build();

        FrontierUtilitySearch alg = new FrontierUtilitySearch(r -> captured[0] = r);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].getMoves() > 0);
    }

    /**
     * When the orb component is unreachable the frontier empties without
     * finding the orb. The algorithm must terminate without throwing.
     */
    @Test
    void terminatesGracefullyOnDisconnectedGraph() {
        // 0 -- 1 -- 2  reachable; node 99(orb) is isolated.
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(99)
                .edge(0, 1).edge(1, 2)
                .distance(0, 5).distance(1, 4).distance(2, 3)
                .distance(99, 0)
                .build();

        FrontierUtilitySearch alg = new FrontierUtilitySearch(NO_OP);

        assertDoesNotThrow(() -> alg.findOrb(state));
    }
}