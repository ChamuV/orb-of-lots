package student.searchalg;

import game.ExplorationState;
import student.benchmark.BenchmarkRecorder;
import student.benchmark.BenchmarkSession;
import student.benchmark.BenchmarkAlgorithmSelector;
import student.benchmark.writer.CsvRunWriter;

import java.nio.file.Path;

/**
 * Base implementation for benchmarked search algorithms.
 *
 * Handles benchmarking and result export so subclasses only need
 * to implement the search behaviour.
 */
public abstract class Algorithm extends AbstractAlgorithm {

    private static final Path BENCHMARK_DIR =
            Path.of("benchmark-data");

    private final BenchmarkSession benchmarkSession;
    private final BenchmarkRecorder benchmarkRecorder;

    protected Algorithm() {
        String algorithmName = System.getProperty(
                BenchmarkAlgorithmSelector.BENCHMARK_NAME_PROPERTY,
                getClass().getSimpleName()
        );

        this.benchmarkSession =
                new BenchmarkSession(algorithmName);

        this.benchmarkRecorder =
                new BenchmarkRecorder(
                        new CsvRunWriter(benchmarkPathFor(algorithmName))
                );
    }

    private Path benchmarkPathFor(String algorithmName) {
        return BENCHMARK_DIR.resolve(algorithmName + ".csv");
    }

    @Override
    public final void findOrb(ExplorationState state) {
        benchmarkSession.start();

        runSearch(state);

        benchmarkSession.stop();

        benchmarkRecorder.record(
                benchmarkSession.getResult()
        );
    }

    protected abstract void runSearch(ExplorationState state);

    @Override
    public String getAlgorithmName() {
        return getClass().getSimpleName();
    }

    @Override
    public BenchmarkSession getBenchmarkSession() {
        return benchmarkSession;
    }

    /**
     * Records a move made during the search.
     */
    protected void recordMove() {
        benchmarkSession.recordMove();
    }
}