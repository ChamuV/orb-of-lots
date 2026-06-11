package student.searchalg;

import student.benchmark.BenchmarkSession;

public interface BenchmarkableAlgorithm {

    String getAlgorithmName();

    BenchmarkSession getBenchmarkSession();
}