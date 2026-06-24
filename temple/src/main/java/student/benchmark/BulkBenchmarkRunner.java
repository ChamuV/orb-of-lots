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

    private static final int SEED_LIMIT = 50;

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
        "GradientFrontierUtilitySearch",
        "CoverageBiasedFrontierUtilitySearch"
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

        System.setProperty("benchmark.algorithm", algorithmName);

        for (long seed : seeds) {
            System.setProperty("benchmark.seed", Long.toString(seed));

            int score = GameState.runNewGame(seed, false);

            System.out.printf("  seed=%d, score=%d%n", seed, score);
        }
    }
}