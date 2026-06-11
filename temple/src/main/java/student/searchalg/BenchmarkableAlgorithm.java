package student.searchalg;

import student.benchmark.BenchmarkSession;

/**
 * Defines an algorithm that exposes benchmark measurements.
 */
public interface BenchmarkableAlgorithm {

    String getAlgorithmName();

    BenchmarkSession getBenchmarkSession();
}