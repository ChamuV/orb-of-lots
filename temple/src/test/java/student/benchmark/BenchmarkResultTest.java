package student.benchmark;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BenchmarkResult}.
 *
 * <p>Verifies default values, field mutation, and that the algorithm name
 * supplied at construction is immutable.
 */
class BenchmarkResultTest {

    @Test
    void algorithmNameIsSetAtConstruction() {
        BenchmarkResult result = new BenchmarkResult("MyAlgorithm");
        assertEquals("MyAlgorithm", result.getAlgorithmName());
    }

    @Test
    void defaultSuccessIsTrue() {
        // BenchmarkResult defaults success=true so that a run that throws
        // before markFailure() is called still appears in analysis.
        BenchmarkResult result = new BenchmarkResult("Alg");
        assertTrue(result.isSuccess());
    }

    @Test
    void movesDefaultsToZeroAndCanBeSet() {
        BenchmarkResult result = new BenchmarkResult("Alg");
        assertEquals(0, result.getMoves());
        result.setMoves(42);
        assertEquals(42, result.getMoves());
    }

    @Test
    void runtimeUsDefaultsToZeroAndCanBeSet() {
        BenchmarkResult result = new BenchmarkResult("Alg");
        assertEquals(0L, result.getRuntimeUs());
        result.setRuntimeUs(9999L);
        assertEquals(9999L, result.getRuntimeUs());
    }

    @Test
    void seedDefaultsToZeroAndCanBeSet() {
        BenchmarkResult result = new BenchmarkResult("Alg");
        assertEquals(0L, result.getSeed());
        result.setSeed(12345L);
        assertEquals(12345L, result.getSeed());
    }

    @Test
    void successCanBeExplicitlySetToFalse() {
        BenchmarkResult result = new BenchmarkResult("Alg");
        result.setSuccess(false);
        assertFalse(result.isSuccess());
    }
}