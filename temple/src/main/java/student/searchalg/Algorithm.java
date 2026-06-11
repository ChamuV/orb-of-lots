package student.searchalg;

import game.ExplorationState;
import student.benchmark.BenchmarkRecorder;
import student.benchmark.BenchmarkResult;
import student.benchmark.BenchmarkSession;
import student.benchmark.writer.BenchmarkWriter;
import student.benchmark.writer.CsvRunWriter;

import java.nio.file.Path;

/**
 * Base implementation for benchmarked search algorithms.
 *
 * Handles benchmarking and result export so subclasses only need
 * to implement the search behaviour.
 */
public abstract class Algorithm extends AbstractAlgorithm {

    /**
     * Default location for benchmark results.
     */
    private static final Path DEFAULT_RUN_OUTPUT =
            Path.of("benchmark-data", "benchmark_runs.csv");

    private final BenchmarkSession benchmarkSession;

    /**
     * Records benchmark results after each run.
     */
    private final BenchmarkRecorder benchmarkRecorder;

    protected Algorithm() {
        this(
                new CsvRunWriter(DEFAULT_RUN_OUTPUT)
        );
    }

    protected Algorithm(
            BenchmarkWriter<BenchmarkResult> benchmarkWriter
    ) {
        this.benchmarkSession =
                new BenchmarkSession(
                        getClass().getSimpleName()
                );

        this.benchmarkRecorder =
                new BenchmarkRecorder(benchmarkWriter);
    }

    /**
     * Runs the search and records benchmark information.
     */
    @Override
    public final void findOrb(ExplorationState state) {
        benchmarkSession.start();

        runSearch(state);

        benchmarkSession.stop();

        benchmarkRecorder.record(
                benchmarkSession.getResult()
        );
    }

    /**
     * Performs the algorithm-specific search logic.
     */
    protected abstract void runSearch(
            ExplorationState state
    );

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