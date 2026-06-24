package student.benchmark;

import student.searchalg.SearchAlgorithm;
import student.searchalg.bfs.BreadthFirstSearch;
import student.searchalg.dfs.AdaptiveHeuristicSearch;
import student.searchalg.dfs.DFS;
import student.searchalg.dfs.GreedyDFS;
import student.searchalg.frontier.CoverageBiasedFrontierUtilitySearch;
import student.searchalg.frontier.FrontierUtilitySearch;
import student.searchalg.frontier.GradientFrontierUtilitySearch;
import student.searchalg.frontier.ReplanningFrontierUtilitySearch;
import student.searchalg.idastar.IterativeDeepeningAStarSearch;
import student.searchalg.random.RandomWalkSearch;
import student.searchalg.rta.RealTimeAStarSearch;

/**
 * Selects an exploration algorithm during bulk benchmark runs.
 *
 * <p>Normal GUI/TXT runs are unaffected: if no benchmark algorithm property is
 * set, Explorer uses its normal final algorithm.
 */
public final class BenchmarkAlgorithmSelector {

    public static final String BENCHMARK_NAME_PROPERTY =
        "benchmark.name";
        
    public static final String ALGORITHM_PROPERTY = "benchmark.algorithm";

    private BenchmarkAlgorithmSelector() {
        // Utility class
    }

    public static SearchAlgorithm selectOrDefault(SearchAlgorithm defaultAlgorithm) {
        String algorithmName = System.getProperty(ALGORITHM_PROPERTY);

        if (algorithmName == null || algorithmName.isBlank()) {
            return defaultAlgorithm;
        }

        return createAlgorithm(algorithmName);
    }

    private static SearchAlgorithm createAlgorithm(String algorithmName) {
        if (algorithmName.startsWith("GradientFrontierUtilitySearch:")) {
            double lambda = parseParameter(algorithmName);
            return new GradientFrontierUtilitySearch(lambda);
        }

        if (algorithmName.startsWith("CoverageBiasedFrontierUtilitySearch:")) {
            double mu = parseParameter(algorithmName);
            return new CoverageBiasedFrontierUtilitySearch(mu);
        }

        return switch (algorithmName) {
            case "DFS" -> new DFS();
            case "GreedyDFS" -> new GreedyDFS();
            case "AdaptiveHeuristicSearch" -> new AdaptiveHeuristicSearch();
            case "BreadthFirstSearch" -> new BreadthFirstSearch();
            case "RandomWalkSearch" -> new RandomWalkSearch();
            case "RealTimeAStarSearch" -> new RealTimeAStarSearch();
            case "IterativeDeepeningAStarSearch" -> new IterativeDeepeningAStarSearch();
            case "FrontierUtilitySearch" -> new FrontierUtilitySearch();
            case "ReplanningFrontierUtilitySearch" -> new ReplanningFrontierUtilitySearch();
            case "GradientFrontierUtilitySearch" -> new GradientFrontierUtilitySearch();
            case "CoverageBiasedFrontierUtilitySearch" -> new CoverageBiasedFrontierUtilitySearch();
            default -> throw new IllegalArgumentException(
                    "Unknown benchmark algorithm: " + algorithmName
            );
        };
    }

    private static double parseParameter(String algorithmName) {
        int separator = algorithmName.indexOf(':');

        if (separator < 0 || separator == algorithmName.length() - 1) {
            throw new IllegalArgumentException(
                    "Missing parameter in benchmark algorithm: " + algorithmName
            );
        }

        return Double.parseDouble(algorithmName.substring(separator + 1));
    }
}