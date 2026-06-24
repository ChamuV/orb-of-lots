package student.benchmark.writer;

import student.benchmark.BenchmarkResult;
import student.benchmark.csv.AbstractCsvHeader;
import student.benchmark.csv.RunCsvHeader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes per-run benchmark results to CSV.
 */
public class CsvRunWriter implements BenchmarkWriter<BenchmarkResult> {

    private static final AbstractCsvHeader HEADER = new RunCsvHeader();

    private final CsvWriter csvWriter;

    public CsvRunWriter(Path outputPath) {
        this.csvWriter = new FileCsvWriter(outputPath);
    }

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