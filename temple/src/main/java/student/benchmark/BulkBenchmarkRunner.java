package student.benchmark;

import game.GameState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs selected exploration algorithms over the same fixed seed list.
 *
 * <p>This class lives in the student package so it does not require changes to
 * the protected main or game packages.
 */
public final class BulkBenchmarkRunner {

    private static final Path SEEDS_FILE =
            Path.of("benchmark-data", "seeds.txt");

    private static final int SEED_LIMIT = 100;

    private static final List<String> ALGORITHMS = List.of(
        "DFS",
        "GreedyDFS",
        "AdaptiveHeuristicSearch",
        "BreadthFirstSearch",
        "RandomWalkSearch",
        "RealTimeAStarSearch",
        "IterativeDeepeningAStarSearch",

        "FrontierUtilitySearch",
        "ReplanningFrontierUtilitySearch",

        "GradientFrontierUtilitySearch:0.5",
        "GradientFrontierUtilitySearch:0.75",
        "GradientFrontierUtilitySearch:1.0",
        "GradientFrontierUtilitySearch:1.25",
        "GradientFrontierUtilitySearch:1.5",
        "GradientFrontierUtilitySearch:2.0",
        "GradientFrontierUtilitySearch:3.0",
        "GradientFrontierUtilitySearch:5.0",
        "GradientFrontierUtilitySearch:10.0",

        "CoverageBiasedFrontierUtilitySearch:0.5",
        "CoverageBiasedFrontierUtilitySearch:1.0",
        "CoverageBiasedFrontierUtilitySearch:1.5",
        "CoverageBiasedFrontierUtilitySearch:2.0",
        "CoverageBiasedFrontierUtilitySearch:3.0",

        "CoverageBiasedFrontierUtilitySearch:4.0",
        "CoverageBiasedFrontierUtilitySearch:4.5",
        "CoverageBiasedFrontierUtilitySearch:5.0",
        "CoverageBiasedFrontierUtilitySearch:5.5",
        "CoverageBiasedFrontierUtilitySearch:6.0",

        "CoverageBiasedFrontierUtilitySearch:10.0"
    );

    private BulkBenchmarkRunner() {
        // Utility class
    }

    public static void main(String[] args) throws IOException {
        List<Long> seeds = readSeeds();

        System.out.printf(
                "Running %d algorithms over %d seeds%n",
                ALGORITHMS.size(),
                seeds.size()
        );

        for (String algorithmName : ALGORITHMS) {
            runAlgorithm(algorithmName, seeds);
        }

        clearBenchmarkProperties();

        System.out.println("Bulk benchmark complete.");
    }

    private static List<Long> readSeeds() throws IOException {
        return Files.readAllLines(SEEDS_FILE)
                .stream()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .limit(SEED_LIMIT)
                .map(Long::parseLong)
                .toList();
    }

    private static void runAlgorithm(String algorithmName, List<Long> seeds) {
        System.out.printf("%nAlgorithm: %s%n", algorithmName);

        System.setProperty(
                BenchmarkAlgorithmSelector.ALGORITHM_PROPERTY,
                algorithmName
        );

        System.setProperty(
                BenchmarkAlgorithmSelector.BENCHMARK_NAME_PROPERTY,
                benchmarkNameFor(algorithmName)
        );

        for (long seed : seeds) {
            System.setProperty("benchmark.seed", Long.toString(seed));

            int score = GameState.runNewGame(seed, false);

            System.out.printf("  seed=%d, score=%d%n", seed, score);
        }
    }

    private static String benchmarkNameFor(String algorithmName) {
        return algorithmName
                .replace(':', '_')
                .replace('.', '_');
    }

    private static void clearBenchmarkProperties() {
        System.clearProperty(BenchmarkAlgorithmSelector.ALGORITHM_PROPERTY);
        System.clearProperty(BenchmarkAlgorithmSelector.BENCHMARK_NAME_PROPERTY);
        System.clearProperty("benchmark.seed");
    }
}