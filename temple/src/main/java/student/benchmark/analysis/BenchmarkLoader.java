package student.benchmark.analysis;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads benchmark run data from CSV files.
 *
 * <p>The loader reads all per-algorithm CSV files from the benchmark data
 * directory and converts each row into a {@link BenchmarkRun}. It ignores
 * non-result files such as {@code seeds.txt}.
 */
public class BenchmarkLoader {

    private static final Path DEFAULT_BENCHMARK_DIR =
            Path.of("../benchmark-data");

    private final Path benchmarkDir;

    /**
     * Creates a loader that reads from the default benchmark data directory.
     */
    public BenchmarkLoader() {
        this(DEFAULT_BENCHMARK_DIR);
    }

    /**
     * Creates a loader that reads benchmark data from the given directory.
     *
     * @param benchmarkDir directory containing benchmark CSV files
     */
    public BenchmarkLoader(Path benchmarkDir) {
        this.benchmarkDir = benchmarkDir;
    }

    /**
     * Loads every benchmark run found in the benchmark directory.
     *
     * @return all benchmark runs loaded from CSV files
     */
    public List<BenchmarkRun> loadRuns() {
        try {
            List<BenchmarkRun> runs = new ArrayList<>();

            for (Path csvFile : benchmarkCsvFiles()) {
                runs.addAll(loadRunsFromFile(csvFile));
            }

            return runs;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load benchmark data from " + benchmarkDir,
                    e
            );
        }
    }

    private List<Path> benchmarkCsvFiles() throws IOException {
        if (Files.notExists(benchmarkDir)) {
            return List.of();
        }

        try (var paths = Files.list(benchmarkDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .sorted()
                    .toList();
        }
    }

    private List<BenchmarkRun> loadRunsFromFile(Path csvFile)
            throws IOException {
        List<String> lines = Files.readAllLines(csvFile);

        if (lines.size() <= 1) {
            return List.of();
        }

        List<BenchmarkRun> runs = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).strip();

            if (!line.isBlank()) {
                runs.add(parseRun(line));
            }
        }

        return runs;
    }

    private BenchmarkRun parseRun(String line) {
        List<String> parts = Splitter.on(',').splitToList(line);

        if (parts.size() != 5) {
            throw new IllegalArgumentException(
                    "Invalid benchmark row: " + line
            );
        }

        return new BenchmarkRun(
                parts.get(0),
                Long.parseLong(parts.get(1)),
                Boolean.parseBoolean(parts.get(2)),
                Integer.parseInt(parts.get(3)),
                Long.parseLong(parts.get(4))
        );
    }
}