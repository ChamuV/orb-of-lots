package student.benchmark;

import student.benchmark.counter.Counter;
import student.benchmark.counter.MoveCounter;
import student.benchmark.timer.NanoTimer;
import student.benchmark.timer.Timer;

/**
 * Coordinates benchmark measurements for a single search algorithm run.
 */
public class BenchmarkSession {

    private final BenchmarkResult result;
    private final Timer timer;
    private final Counter moveCounter;

    public BenchmarkSession(String algorithmName) {
        this(
                algorithmName,
                new NanoTimer(),
                new MoveCounter()
        );
    }

    public BenchmarkSession(
            String algorithmName,
            Timer timer,
            Counter moveCounter
    ) {
        this.result = new BenchmarkResult(algorithmName);
        this.timer = timer;
        this.moveCounter = moveCounter;
    }

    public void setSeed(long seed) {
        result.setSeed(seed);
    }

    public void markSuccess() {
        result.setSuccess(true);
    }

    public void markFailure() {
        result.setSuccess(false);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
        result.setRuntimeMs(timer.getElapsedMilliseconds());
        result.setMoves(moveCounter.getValue());
    }

    public void recordMove() {
        moveCounter.increment();
    }

    public BenchmarkResult getResult() {
        return result;
    }
}