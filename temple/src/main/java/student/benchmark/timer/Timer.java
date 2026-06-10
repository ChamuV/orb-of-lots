package student.benchmark.timer;

/**
 * Abstraction for measuring elapsed runtime.
 */
public interface Timer {

    void start();
    void stop();
    
    long getElapsedMilliseconds();
}