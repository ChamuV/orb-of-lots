package student.benchmark.counter;

/**
 * Abstraction for counting benchmark events.
 */
public interface Counter {

    void increment();

    int getValue();
}