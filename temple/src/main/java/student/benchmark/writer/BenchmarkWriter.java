package student.benchmark.writer;

/**
 * Common abstraction for benchmark output writers.
 *
 * @param <T> type of benchmark data written
 */
public interface BenchmarkWriter<T> {

    /**
     * Writes the given benchmark data to the output destination.
     *
     * @param data the benchmark data to write
     */
    void write(T data);

}