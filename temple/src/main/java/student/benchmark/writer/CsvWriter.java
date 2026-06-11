package student.benchmark.writer;

import java.io.IOException;

public interface CsvWriter {

    void writeHeaderIfNeeded(String header) throws IOException;

    void appendLine(String line) throws IOException;
}