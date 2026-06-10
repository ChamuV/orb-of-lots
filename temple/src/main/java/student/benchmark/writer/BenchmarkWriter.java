package student.benchmark.writer;

/**
 * Common abstraction for benchmark output writers.
 *
 * @param <T> type of benchmark data written
 */
public interface BenchmarkWriter<T> {

    void write(T data);

}