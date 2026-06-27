package student.searchalg.frontier;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GradientFrontierUtilitySearch}.
 *
 * <p>Verifies parameter validation, that a positive gradient gain reduces
 * the score, and that higher lambda amplifies the gradient effect.
 */
class GradientFrontierUtilitySearchTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = result -> {};

    @Test
    void nonPositiveLambdaThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new GradientFrontierUtilitySearch(0.0, NO_OP));
        assertThrows(IllegalArgumentException.class,
                () -> new GradientFrontierUtilitySearch(-1.0, NO_OP));
    }

    @Test
    void positiveGradientGainReducesScore() {
        GradientFrontierUtilitySearch alg =
                new GradientFrontierUtilitySearch(1.0, NO_OP);

        // current is at distance 10, candidate is at distance 5 — gain of 5
        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb = Map.of(0L, 10, 1L, 5);

        double score = alg.score(1L, 0L, travelCost, distToOrb);

        // score = travel - lambda * gain = 3 - 1.0 * 5 = -2
        assertEquals(-2.0, score, 0.001);
    }

    @Test
    void higherLambdaAmplifiesGradientEffect() {
        GradientFrontierUtilitySearch lowLambda =
                new GradientFrontierUtilitySearch(1.0, NO_OP);
        GradientFrontierUtilitySearch highLambda =
                new GradientFrontierUtilitySearch(5.0, NO_OP);

        Map<Long, Integer> travelCost = Map.of(1L, 3);
        Map<Long, Integer> distToOrb = Map.of(0L, 10, 1L, 5);

        double scoreLow = lowLambda.score(1L, 0L, travelCost, distToOrb);
        double scoreHigh = highLambda.score(1L, 0L, travelCost, distToOrb);

        assertTrue(scoreHigh < scoreLow);
    }
}