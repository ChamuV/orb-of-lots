package student.benchmark.writer;

import student.benchmark.BenchmarkSummary;
import student.benchmark.csv.AbstractCsvHeader;
import student.benchmark.csv.SummaryCsvHeader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes aggregate benchmark summaries to CSV.
 */
public class CsvSummaryWriter implements BenchmarkWriter<BenchmarkSummary> {

    private static final AbstractCsvHeader HEADER = new SummaryCsvHeader();

    private final CsvWriter csvWriter;

    public CsvSummaryWriter(Path outputPath) {
        this.csvWriter = new FileCsvWriter(outputPath);
    }

    @Override
    public void write(BenchmarkSummary summary) {
        try {
            csvWriter.writeHeaderIfNeeded(HEADER.value());
            csvWriter.appendLine(formatRow(summary));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write benchmark summary CSV", e);
        }
    }

    private String formatRow(BenchmarkSummary summary) {
        return String.format(
                "%s,%d,%.2f,%d,%d,%.2f",
                summary.getAlgorithmName(),
                summary.getRunCount(),
                summary.getAverageMoves(),
                summary.getBestMoves(),
                summary.getWorstMoves(),
                summary.getAverageRuntimeUs()
        );
    }
}