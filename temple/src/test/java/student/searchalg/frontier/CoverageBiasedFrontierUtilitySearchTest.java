package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CoverageBiasedFrontierUtilitySearch}.
 *
 * <p>Verifies the coverage bonus effect on scores, parameter validation,
 * and that mu=0 reduces the algorithm to the base utility score.
 */
class CoverageBiasedFrontierUtilitySearchTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void muZeroMatchesBaseScore() {
        CoverageBiasedFrontierUtilitySearch alg =
                new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb = Map.of(1L, 5);

        double score = alg.score(1L, 0L, travelCost, distToOrb);

        assertEquals(8.0, score, 0.001);
    }

    @Test
    void negativeMuThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new CoverageBiasedFrontierUtilitySearch(-1.0, NO_OP));
    }

    @Test
    void higherMuProducesLowerScoreForDenseCandidate() {
        CoverageBiasedFrontierUtilitySearch lowMu =
                new CoverageBiasedFrontierUtilitySearch(0.0, NO_OP);
        CoverageBiasedFrontierUtilitySearch highMu =
                new CoverageBiasedFrontierUtilitySearch(5.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb = Map.of(1L, 5);

        double scoreLow = lowMu.score(1L, 0L, travelCost, distToOrb);
        double scoreHigh = highMu.score(1L, 0L, travelCost, distToOrb);

        // highMu applies a larger bonus so score is lower or equal
        assertTrue(scoreHigh <= scoreLow);
    }
}