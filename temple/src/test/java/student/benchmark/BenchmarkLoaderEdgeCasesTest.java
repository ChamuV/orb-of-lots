package student.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import student.benchmark.analysis.BenchmarkLoader;
import student.benchmark.analysis.BenchmarkRun;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for {@link BenchmarkLoader}: missing directory, header-only
 * files, non-CSV files being skipped, multi-file loading, field parsing,
 * and malformed row rejection.
 */
class BenchmarkLoaderEdgeCasesTest {

    private static final String HEADER = "algorithm,seed,success,moves,runtime_us\n";

    @Test
    void returnsEmptyListWhenDirectoryDoesNotExist(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("missing-dir");

        List<BenchmarkRun> runs = new BenchmarkLoader(nonExistent).loadRuns();

        assertTrue(runs.isEmpty());
    }

    @Test
    void skipsHeaderOnlyFile(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("AlgA.csv"), HEADER);

        assertTrue(new BenchmarkLoader(tempDir).loadRuns().isEmpty());
    }

    @Test
    void ignoresNonCsvFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("seeds.txt"), "12345\n67890\n");
        Files.writeString(tempDir.resolve("notes.md"), "# Notes");

        assertTrue(new BenchmarkLoader(tempDir).loadRuns().isEmpty());
    }

    @Test
    void loadsRunsFromMultipleCsvFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("AlgA.csv"),
                HEADER + "AlgA,1,true,10,100\nAlgA,2,true,20,200\n");
        Files.writeString(tempDir.resolve("AlgB.csv"),
                HEADER + "AlgB,1,true,15,150\n");

        assertEquals(3, new BenchmarkLoader(tempDir).loadRuns().size());
    }

    @Test
    void parsesAllFieldsCorrectly(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("AlgX.csv"),
                HEADER + "AlgX,999,false,42,1234\n");

        BenchmarkRun run = new BenchmarkLoader(tempDir).loadRuns().get(0);

        assertEquals("AlgX", run.algorithm());
        assertEquals(999L,   run.seed());
        assertFalse(run.success());
        assertEquals(42,     run.moves());
        assertEquals(1234L,  run.runtimeUs());
    }

    @Test
    void throwsOnMalformedRow(@TempDir Path tempDir) throws IOException {
        // Only 3 columns instead of 5
        Files.writeString(tempDir.resolve("Bad.csv"), HEADER + "AlgZ,99,true\n");

        assertThrows(IllegalArgumentException.class,
                () -> new BenchmarkLoader(tempDir).loadRuns());
    }
}