package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FrontierUtilitySearch}.
 *
 * <p>Verifies that the base utility score correctly combines orb distance
 * and travel cost, and that the lowest-scoring candidate is preferred.
 */
class FrontierUtilitySearchTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    private final FrontierUtilitySearch alg = new FrontierUtilitySearch(NO_OP);

    @Test
    void scoreIsOrbDistancePlusTravelCost() {
        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb = Map.of(1L, 5);

        double score = alg.score(1L, 0L, travelCost, distToOrb);

        assertEquals(8.0, score, 0.001);
    }

    @Test
    void lowerOrbDistanceProducesLowerScore() {
        Map<Long, Integer> travelCost = Map.of(1L, 2, 2L, 2);
        Map<Long, Integer> distToOrb = Map.of(1L, 10, 2L, 3);

        double scoreA = alg.score(1L, 0L, travelCost, distToOrb);
        double scoreB = alg.score(2L, 0L, travelCost, distToOrb);

        assertTrue(scoreB < scoreA);
    }

    @Test
    void lowerTravelCostProducesLowerScore() {
        Map<Long, Integer> travelCost = Map.of(1L, 1, 2L, 10);
        Map<Long, Integer> distToOrb = Map.of(1L, 5, 2L, 5);

        double scoreA = alg.score(1L, 0L, travelCost, distToOrb);
        double scoreB = alg.score(2L, 0L, travelCost, distToOrb);

        assertTrue(scoreA < scoreB);
    }
}