package student.searchalg;

import game.ExplorationState;
import student.benchmark.BenchmarkAlgorithmSelector;
import student.benchmark.BenchmarkResult;
import student.benchmark.BenchmarkSession;
import student.benchmark.writer.BenchmarkWriter;
import student.benchmark.writer.CsvRunWriter;

import java.nio.file.Path;

/**
 * Abstract base class for search algorithms that record benchmark measurements.
 *
 * <p>Uses a template-method pattern: {@link #findOrb(ExplorationState)} handles
 * timing, move counting, success detection, and result export, while subclasses
 * implement only {@link #runSearch(ExplorationState)} to define the search
 * behaviour.
 *
 * <p>Benchmark results are written to {@code benchmark-data/<algorithm>.csv}
 * by default. A custom {@link BenchmarkWriter} may be supplied via the
 * protected constructor to redirect or suppress output, for example during
 * testing.
 */
public abstract class Algorithm
        implements SearchAlgorithm, BenchmarkableAlgorithm {

    /**
     * Directory for benchmark CSV output, resolved relative to the Gradle
     * working directory (the project root).
     */
    private static final Path BENCHMARK_DIR = Path.of("benchmark-data");

    private final BenchmarkSession benchmarkSession;
    private final BenchmarkWriter<BenchmarkResult> benchmarkWriter;

    /**
     * Creates an algorithm that writes benchmark results to the default CSV
     * location.
     */
    protected Algorithm() {
        this(null);
    }

    /**
     * Creates an algorithm with the given benchmark writer.
     *
     * <p>If {@code benchmarkWriter} is {@code null}, a {@link CsvRunWriter}
     * targeting {@code benchmark-data/<algorithm>.csv} is used instead.
     *
     * @param benchmarkWriter writer used to record benchmark results, or
     *                        {@code null} to use the default CSV writer
     */
    protected Algorithm(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        String algorithmName = System.getProperty(
                BenchmarkAlgorithmSelector.BENCHMARK_NAME_PROPERTY,
                getClass().getSimpleName()
        );

        this.benchmarkSession = new BenchmarkSession(algorithmName);

        this.benchmarkWriter = benchmarkWriter != null
                ? benchmarkWriter
                : new CsvRunWriter(benchmarkPathFor(algorithmName));
    }

    private Path benchmarkPathFor(String algorithmName) {
        return BENCHMARK_DIR.resolve(algorithmName + ".csv");
    }

    /**
     * Executes the search strategy and records benchmark measurements.
     *
     * <p>This method is {@code final}. It starts the benchmark session,
     * delegates the search to {@link #runSearch(ExplorationState)}, then
     * records whether the Orb was successfully reached before writing the
     * benchmark result.
     *
     * @param state the current exploration state
     */
    @Override
    public final void findOrb(ExplorationState state) {
        benchmarkSession.start();

        runSearch(state);

        benchmarkSession.stop();

        if (state.getDistanceToTarget() == 0) {
            benchmarkSession.markSuccess();
        } else {
            benchmarkSession.markFailure();
        }

        benchmarkWriter.write(benchmarkSession.getResult());
    }

    /**
     * Implements the search behaviour for this algorithm.
     *
     * <p>Subclasses should navigate the explorer to the Orb using only the
     * information available through {@code state}. Each call to
     * {@code state.moveTo(...)} must be accompanied by a call to
     * {@link #recordMove()} so that benchmark statistics remain accurate.
     *
     * @param state the current exploration state
     */
    protected abstract void runSearch(ExplorationState state);

    /** Returns the simple class name of this algorithm. */
    @Override
    public String getAlgorithmName() {
        return getClass().getSimpleName();
    }

    /** Returns the benchmark session for this algorithm instance. */
    @Override
    public BenchmarkSession getBenchmarkSession() {
        return benchmarkSession;
    }

    /**
     * Records a single movement made by the search algorithm.
     *
     * <p>Subclasses should call this exactly once for each invocation of
     * {@code state.moveTo(...)}.
     */
    protected void recordMove() {
        benchmarkSession.recordMove();
    }
}