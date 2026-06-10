package student.benchmark.timer;

/**
 * Timer implementation based on System.nanoTime().
 */
public class NanoTimer implements Timer {
    
    private long startTimeNs;
    private long endTimeNs;

    @Override
    public void start() {
        startTimeNs = System.nanoTime();
    }

    @Override
    public void stop() {
        endTimeNs = System.nanoTime();
    }

    @Override
    public long getElapsedMilliseconds() {
        return (endTimeNs - startTimeNs) / 1_000_000;
    }
}