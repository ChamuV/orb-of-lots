package student.searchalg;

import game.ExplorationState;
import student.benchmark.BenchmarkAlgorithmSelector;
import student.benchmark.BenchmarkResult;
import student.benchmark.BenchmarkSession;
import student.benchmark.writer.BenchmarkWriter;
import student.benchmark.writer.CsvRunWriter;

import java.nio.file.Path;

/**
 * Base implementation for benchmarked search algorithms.
 *
 * <p>Handles benchmarking and result export so subclasses only need
 * to implement the search behaviour.
 */
public abstract class Algorithm
        implements SearchAlgorithm, BenchmarkableAlgorithm {

    private static final Path BENCHMARK_DIR =
            Path.of("benchmark-data");

    private final BenchmarkSession benchmarkSession;
    private final BenchmarkWriter<BenchmarkResult> benchmarkWriter;

    protected Algorithm() {
        this(null);
    }

    /**
     * Creates an algorithm with an injected benchmark writer.
     *
     * <p>This constructor is package-private so tests in the same package can
     * provide a no-op writer without causing filesystem output.
     *
     * @param benchmarkWriter writer used to record benchmark results, or null
     *                        to use the default CSV writer
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