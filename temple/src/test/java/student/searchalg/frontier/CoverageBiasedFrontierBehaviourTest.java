package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end behaviour tests for {@link CoverageBiasedFrontierUtilitySearch}
 * on graphs where frontier density actually varies between candidates.
 */
class CoverageBiasedFrontierBehaviourTest {

    /**
     * Linear graph: only one frontier node exists at a time, so density is
     * always 0 and the algorithm reduces to the base utility score.
     */
    @Test
    void findsOrbOnLinearGraphWhereDensityIsAlwaysZero() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(1, 2).edge(2, 3)
                .distance(0, 3).distance(1, 2).distance(2, 1).distance(3, 0)
                .build();

        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(5.0, r -> {});
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Star graph: after visiting node 0, all of 1, 2, 3 enter the frontier
     * simultaneously, giving non-zero densities. A high mu should still
     * produce a valid run that finds the orb.
     */
    @Test
    void findsOrbOnStarGraphWithHighMu() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(0, 2).edge(0, 3)
                .distance(0, 1).distance(1, 4).distance(2, 4).distance(3, 0)
                .build();

        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(10.0, r -> captured[0] = r);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertTrue(captured[0].isSuccess());
    }

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
}