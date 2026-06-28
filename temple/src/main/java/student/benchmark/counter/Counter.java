package student.benchmark.counter;

/**
 * Abstraction for counting benchmark events.
 */
public interface Counter {

    /** Increments the count by one. */
    void increment();

    /**
     * Returns the current count.
     *
     * @return the current count value
     */
    int getValue();
}