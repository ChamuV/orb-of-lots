package student.benchmark;

import student.benchmark.writer.BenchmarkWriter;

/**
 * Records benchmark results using the configured writer.
 */
public class BenchmarkRecorder {

    private final BenchmarkWriter<BenchmarkResult> runWriter;

    public BenchmarkRecorder(BenchmarkWriter<BenchmarkResult> runWriter) {
        this.runWriter = runWriter;
    }

    public void record(BenchmarkResult result) {
        runWriter.write(result);
    }
}