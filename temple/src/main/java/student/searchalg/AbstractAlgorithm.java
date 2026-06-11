package student.searchalg;

import student.benchmark.BenchmarkSession;

/**
 * Base class for search algorithms that support benchmarking.
 */
public abstract class AbstractAlgorithm
        implements SearchAlgorithm, BenchmarkableAlgorithm {

    private final BenchmarkSession benchmarkSession;

    protected AbstractAlgorithm() {
        benchmarkSession = new BenchmarkSession(getClass().getSimpleName());
    }

    @Override
    public String getAlgorithmName() {
        return getClass().getSimpleName();
    }

    @Override
    public BenchmarkSession getBenchmarkSession() {
        return benchmarkSession;
    }

    protected void startBenchmark() {
        benchmarkSession.start();
    }

    protected void stopBenchmark() {
        benchmarkSession.stop();
    }

    protected void recordMove() {
        benchmarkSession.recordMove();
    }
}