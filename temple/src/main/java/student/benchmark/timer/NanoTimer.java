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
    public long getElapsedMicroseconds() {
        return (endTimeNs - startTimeNs) / 1_000;
    }
}