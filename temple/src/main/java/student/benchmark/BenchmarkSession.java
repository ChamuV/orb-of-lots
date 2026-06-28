package student.benchmark;

import student.benchmark.counter.Counter;
import student.benchmark.counter.MoveCounter;
import student.benchmark.timer.NanoTimer;
import student.benchmark.timer.Timer;

/**
 * Coordinates benchmark measurements for a single search algorithm run.
 *
 * <p>Wraps a {@link BenchmarkResult}, a {@link Timer}, and a {@link Counter}
 * to collect move counts and wall-clock runtime in one place. The
 * production constructor uses {@link NanoTimer} and {@link MoveCounter};
 * the package-private constructor accepts injected collaborators for testing.
 */
public class BenchmarkSession {

    private final BenchmarkResult result;
    private final Timer timer;
    private final Counter moveCounter;

    /**
     * Creates a session using the default {@link NanoTimer} and {@link MoveCounter}.
     *
     * @param algorithmName name of the algorithm being benchmarked
     */
    public BenchmarkSession(String algorithmName) {
        this(
                algorithmName,
                new NanoTimer(),
                new MoveCounter()
        );
    }

    /**
     * Creates a session with injected timer and counter, for testing.
     *
     * @param algorithmName name of the algorithm being benchmarked
     * @param timer         timer used to measure wall-clock runtime
     * @param moveCounter   counter used to track movement steps
     */
    BenchmarkSession(
            String algorithmName,
            Timer timer,
            Counter moveCounter
    ) {
        this.result = new BenchmarkResult(algorithmName);
        this.timer = timer;
        this.moveCounter = moveCounter;
    }

    /**
     * Records the seed for the current run on the result.
     *
     * @param seed the cavern seed being benchmarked
     */
    public void setSeed(long seed) {
        result.setSeed(seed);
    }

    /** Records that the algorithm successfully located the Orb. */
    public void markSuccess() {
        result.setSuccess(true);
    }

    /** Records that the algorithm did not locate the Orb. */
    public void markFailure() {
        result.setSuccess(false);
    }

    /** Starts the wall-clock timer for this run. */
    public void start() {
        timer.start();
    }

    /**
     * Stops the timer and flushes elapsed runtime and move count to the result.
     */
    public void stop() {
        timer.stop();
        result.setRuntimeUs(timer.getElapsedMicroseconds());
        result.setMoves(moveCounter.getValue());
    }

    /** Increments the move counter by one. */
    public void recordMove() {
        moveCounter.increment();
    }

    /**
     * Returns the result accumulated during this session.
     *
     * @return the benchmark result for this run
     */
    public BenchmarkResult getResult() {
        return result;
    }
}