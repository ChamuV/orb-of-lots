package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CoverageBiasedFrontierUtilitySearch}.
 *
 * <p>Covers parameter validation, exact score arithmetic, the relationship
 * between mu and the coverage bonus, and the degenerate case where mu=0
 * reduces the algorithm to the base utility score.
 */
class CoverageBiasedFrontierUtilitySearchTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void negativeMuThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new CoverageBiasedFrontierUtilitySearch(-1.0, NO_OP));
    }

    @Test
    void negativeMuJustBelowZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new CoverageBiasedFrontierUtilitySearch(-0.001, NO_OP));
    }

    @Test
    void muZeroIsAccepted() {
        assertDoesNotThrow(() ->
                new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP));
    }

    /**
     * mu=0 disables the coverage term entirely.
     * score = travelCost + orbDistance = 3 + 5 = 8
     */
    @Test
    void muZeroMatchesBaseScore() {
        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(1L, 5);

        assertEquals(8.0, alg.score(1L, 0L, travelCost, distToOrb), 0.001);
    }

    /**
     * A positive mu must produce a score no greater than the mu=0 score,
     * because the coverage term is subtracted (it is a bonus, not a penalty).
     */
    @Test
    void positiveMuScoreIsNoGreaterThanBaseScore() {
        CoverageBiasedFrontierUtilitySearch base =
                new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP);
        CoverageBiasedFrontierUtilitySearch biased =
                new CoverageBiasedFrontierUtilitySearch(5.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(1L, 5);

        assertTrue(biased.score(1L, 0L, travelCost, distToOrb)
                <= base.score(1L, 0L, travelCost, distToOrb));
    }

    /**
     * Increasing mu reduces the score monotonically for the same candidate.
     */
    @Test
    void higherMuProducesLowerOrEqualScore() {
        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb  = Map.of(1L, 5);

        double score1 = new CoverageBiasedFrontierUtilitySearch(1.0, NO_OP)
                .score(1L, 0L, travelCost, distToOrb);
        double score5 = new CoverageBiasedFrontierUtilitySearch(5.0, NO_OP)
                .score(1L, 0L, travelCost, distToOrb);
        double score10 = new CoverageBiasedFrontierUtilitySearch(10.0, NO_OP)
                .score(1L, 0L, travelCost, distToOrb);

        assertTrue(score5 <= score1);
        assertTrue(score10 <= score5);
    }

    /**
     * Two candidates with equal travel cost and equal orb distance; the one
     * with more discovered neighbours should receive the lower score under a
     * positive mu. This verifies that the coverage density term is actually
     * influencing candidate ranking.
     *
     * <p>Because {@code score()} operates on a per-candidate basis, we can only
     * confirm that varying mu changes the score — the relative ranking between
     * two candidates depends on their densities as computed internally from the
     * discovered graph. We therefore verify the monotone mu property and rely
     * on {@link CoverageBiasedFrontierBehaviourTest} for ranking verification.
     */
    @Test
    void scoreDecreasesAsLambdaIncreases() {
        Map<Long, Integer> travelCost = Map.of(1L, 4);
        Map<Long, Integer> distToOrb  = Map.of(1L, 4);

        double low  = new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP)
                .score(1L, 0L, travelCost, distToOrb);
        double high = new CoverageBiasedFrontierUtilitySearch(3.0, NO_OP)
                .score(1L, 0L, travelCost, distToOrb);

        assertTrue(high <= low);
    }
}