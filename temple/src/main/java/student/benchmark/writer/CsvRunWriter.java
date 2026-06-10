package student.benchmark.writer;

import student.benchmark.BenchmarkResult;
import student.benchmark.csv.AbstractCsvHeader;
import student.benchmark.csv.RunCsvHeader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes per-run benchmark results to CSV.
 */
public class CsvRunWriter extends AbstractCsvWriter<BenchmarkResult> {

    private static final AbstractCsvHeader HEADER = new RunCsvHeader();

    public CsvRunWriter(Path outputPath) {
        super(outputPath);
    }

    @Override
    public void write(BenchmarkResult result) {
        try {
            writeHeaderIfNeeded(HEADER.value());
            appendLine(formatRow(result));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write benchmark run CSV", e);
        }
    }

    private String formatRow(BenchmarkResult result) {
        return String.format(
                "%s,%d,%d",
                result.getAlgorithmName(),
                result.getMoves(),
                result.getRuntimeMs()
        );
    }
}