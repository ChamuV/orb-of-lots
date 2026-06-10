package student.benchmark.writer;

import student.benchmark.BenchmarkSummary;
import student.benchmark.csv.AbstractCsvHeader;
import student.benchmark.csv.SummaryCsvHeader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes aggregate benchmark summaries to CSV.
 */
public class CsvSummaryWriter extends AbstractCsvWriter<BenchmarkSummary> {

    private static final AbstractCsvHeader HEADER = new SummaryCsvHeader();

    public CsvSummaryWriter(Path outputPath) {
        super(outputPath);
    }

    @Override
    public void write(BenchmarkSummary summary) {
        try {
            writeHeaderIfNeeded(HEADER.value());
            appendLine(formatRow(summary));
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
                summary.getAverageRuntimeMs()
        );
    }
}