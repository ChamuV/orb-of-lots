package student.benchmark.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Shared CSV-writing behaviour.
 *
 * @param <T> type of benchmark data written
 */
public abstract class AbstractCsvWriter<T> implements BenchmarkWriter<T> {

    private final Path outputPath;

    protected AbstractCsvWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    protected void writeHeaderIfNeeded(String header) throws IOException {
        if (Files.notExists(outputPath) || Files.size(outputPath) == 0) {
            appendLine(header);
        }
    }

    protected void appendLine(String line) throws IOException {
        Files.writeString(
                outputPath,
                line + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}