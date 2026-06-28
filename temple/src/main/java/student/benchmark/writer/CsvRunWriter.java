package student.benchmark.writer;

import student.benchmark.BenchmarkResult;
import student.benchmark.csv.AbstractCsvHeader;
import student.benchmark.csv.RunCsvHeader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes per-run benchmark results to CSV.
 *
 * <p>Each call to {@link #write} appends one row containing the algorithm
 * name, seed, success flag, move count, and runtime in microseconds.
 * The header row is written automatically on first use.
 */
public class CsvRunWriter implements BenchmarkWriter<BenchmarkResult> {

    private static final AbstractCsvHeader HEADER = new RunCsvHeader();

    private final CsvWriter csvWriter;

    /**
     * Creates a writer that appends results to the given file path.
     *
     * @param outputPath path to the CSV output file; parent directories are
     *                   created if they do not exist
     */
    public CsvRunWriter(Path outputPath) {
        this.csvWriter = new FileCsvWriter(outputPath);
    }

    /**
     * Appends one result row to the CSV file.
     *
     * @param result the benchmark result to record
     * @throws IllegalStateException if the file cannot be written
     */
    @Override
    public void write(BenchmarkResult result) {
        try {
            csvWriter.writeHeaderIfNeeded(HEADER.value());
            csvWriter.appendLine(formatRow(result));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write benchmark run CSV", e);
        }
    }

    private String formatRow(BenchmarkResult result) {
        return String.format(
                "%s,%d,%b,%d,%d",
                result.getAlgorithmName(),
                result.getSeed(),
                result.isSuccess(),
                result.getMoves(),
                result.getRuntimeUs()
        );
    }
}