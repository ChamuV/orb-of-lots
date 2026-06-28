package student.benchmark.writer;

import java.io.IOException;

/**
 * Low-level abstraction for writing lines to a CSV file.
 *
 * <p>Implementations are responsible for file creation, appending, and
 * ensuring the header row is written exactly once.
 */
public interface CsvWriter {

    /**
     * Writes the header row if the output file is new or empty.
     *
     * @param header the comma-separated column header string
     * @throws IOException if the file cannot be written
     */
    void writeHeaderIfNeeded(String header) throws IOException;

    /**
     * Appends a line of text followed by a system line separator.
     *
     * @param line the line to append
     * @throws IOException if the file cannot be written
     */
    void appendLine(String line) throws IOException;
}