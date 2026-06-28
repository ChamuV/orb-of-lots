package student.benchmark.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Writes CSV lines to a file.
 */
public class FileCsvWriter implements CsvWriter {

    private final Path outputPath;

    /**
     * Creates a writer that appends to the given file path.
     *
     * @param outputPath path to the CSV output file
     */
    public FileCsvWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void writeHeaderIfNeeded(String header) throws IOException {
        ensureOutputDirectoryExists();

        if (Files.notExists(outputPath) || Files.size(outputPath) == 0) {
            appendLine(header);
        }
    }

    @Override
    public void appendLine(String line) throws IOException {
        ensureOutputDirectoryExists();

        Files.writeString(
                outputPath,
                line + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private void ensureOutputDirectoryExists() throws IOException {
        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}