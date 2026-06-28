package student.benchmark.timer;

/**
 * Abstraction for measuring elapsed runtime.
 */
public interface Timer {

    /** Starts the timer. */
    void start();

    /** Stops the timer. */
    void stop();

    /**
     * Returns the elapsed time between the last {@link #start} and
     * {@link #stop} calls, in microseconds.
     *
     * @return elapsed time in microseconds
     */
    long getElapsedMicroseconds();
}