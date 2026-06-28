package student.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import student.benchmark.analysis.BenchmarkLoader;
import student.benchmark.analysis.BenchmarkRun;
import student.benchmark.counter.Counter;
import student.benchmark.timer.Timer;
import student.benchmark.writer.CsvRunWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the benchmark recording and loading pipeline.
 *
 * <p>Verifies that a {@link BenchmarkSession} result written by
 * {@link CsvRunWriter} can be read back correctly by {@link BenchmarkLoader},
 * covering the full write-then-read cycle without relying on the filesystem
 * outside a temporary directory.
 */
class BenchmarkPipelineTest {

    private static class FixedTimer implements Timer {
        @Override public void start() {}
        @Override public void stop() {}
        @Override public long getElapsedMicroseconds() { return 500L; }
    }

    private static class FixedCounter implements Counter {
        private int count;
        @Override public void increment() { count++; }
        @Override public int getValue() { return count; }
    }

    @Test
    void writtenResultCanBeReadBackByLoader(@TempDir Path tempDir) {
        Path csvPath = tempDir.resolve("TestAlg.csv");

        BenchmarkSession session = new BenchmarkSession("TestAlg", new FixedTimer(), new FixedCounter());
        session.setSeed(42L);
        session.start();
        session.recordMove();
        session.recordMove();
        session.stop();
        session.markSuccess();

        new CsvRunWriter(csvPath).write(session.getResult());

        BenchmarkLoader loader = new BenchmarkLoader(tempDir);
        List<BenchmarkRun> runs = loader.loadRuns();

        assertEquals(1, runs.size());
        BenchmarkRun run = runs.get(0);
        assertEquals("TestAlg", run.algorithm());
        assertEquals(42L, run.seed());
        assertEquals(2, run.moves());
        assertTrue(run.success());
        assertEquals(500L, run.runtimeUs());
    }

    @Test
    void failedRunIsRecordedCorrectly(@TempDir Path tempDir) {
        Path csvPath = tempDir.resolve("TestAlg.csv");

        BenchmarkSession session = new BenchmarkSession("TestAlg", new FixedTimer(), new FixedCounter());
        session.start();
        session.stop();
        session.markFailure();

        new CsvRunWriter(csvPath).write(session.getResult());

        BenchmarkLoader loader = new BenchmarkLoader(tempDir);
        List<BenchmarkRun> run = loader.loadRuns();

        assertFalse(run.get(0).success());
    }

    @Test
    void multipleRunsAreAllRecorded(@TempDir Path tempDir) {
        Path csvPath = tempDir.resolve("TestAlg.csv");
        CsvRunWriter writer = new CsvRunWriter(csvPath);

        for (int i = 0; i < 5; i++) {
            BenchmarkSession session = new BenchmarkSession("TestAlg", new FixedTimer(), new FixedCounter());
            session.start();
            session.stop();
            session.markSuccess();
            writer.write(session.getResult());
        }

        List<BenchmarkRun> runs = new BenchmarkLoader(tempDir).loadRuns();
        assertEquals(5, runs.size());
    }

    @Test
void headerIsWrittenExactlyOnceAcrossMultipleRuns(@TempDir Path tempDir) throws IOException {
    Path csvPath = tempDir.resolve("AlgA.csv");
    CsvRunWriter writer = new CsvRunWriter(csvPath);

    for (int i = 0; i < 3; i++) {
        BenchmarkSession session = new BenchmarkSession("AlgA", new FixedTimer(), new FixedCounter());
        session.start();
        session.stop();
        session.markSuccess();
        writer.write(session.getResult());
    }

    long headerLineCount = Files.lines(csvPath)
            .filter(line -> line.startsWith("algorithm"))
            .count();

    assertEquals(1, headerLineCount, "Header should appear exactly once regardless of run count");
}

    @Test
    void csvRowHasCorrectColumnOrder(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("AlgA.csv");

        BenchmarkSession session = new BenchmarkSession("AlgA", new FixedTimer(), new FixedCounter());
        session.setSeed(77L);
        session.start();
        session.recordMove();
        session.stop();
        session.markSuccess();
        new CsvRunWriter(csvPath).write(session.getResult());

        List<String> lines = Files.readAllLines(csvPath);
        // lines.get(0) is the header; lines.get(1) is the data row
        String[] parts = lines.get(1).split(",");

        assertEquals("AlgA",  parts[0]); // algorithm
        assertEquals("77",    parts[1]); // seed
        assertEquals("true",  parts[2]); // success
        assertEquals("1",     parts[3]); // moves
        // parts[4] is runtime_us — only assert it's a valid long, not its value
        assertDoesNotThrow(() -> Long.parseLong(parts[4]));
    }


}