package student.benchmark.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes aggregate benchmark statistics from individual benchmark runs.
 */
public class BenchmarkAnalyzer {

    /**
     * Computes one statistics row per algorithm.
     *
     * <p>Runs with zero moves are treated as failed/invalid runs. This handles
     * algorithms such as the current BFS implementation, which can return at
     * the wrong location and record zero moves.
     *
     * @param runs benchmark runs loaded from CSV files
     * @return per-algorithm statistics sorted by mean move count
     */
    public List<BenchmarkStatistics> analyze(List<BenchmarkRun> runs) {
        Map<String, List<BenchmarkRun>> runsByAlgorithm =
                runs.stream()
                        .collect(Collectors.groupingBy(BenchmarkRun::algorithm));

        List<BenchmarkStatistics> statistics = new ArrayList<>();

        for (Map.Entry<String, List<BenchmarkRun>> entry : runsByAlgorithm.entrySet()) {
            statistics.add(analyzeAlgorithm(entry.getKey(), entry.getValue()));
        }

        statistics.sort(Comparator.comparingDouble(BenchmarkStatistics::meanMoves));
        return statistics;
    }

    private BenchmarkStatistics analyzeAlgorithm(
            String algorithm,
            List<BenchmarkRun> runs
    ) {
        List<BenchmarkRun> successfulRuns =
                runs.stream()
                        .filter(this::isSuccessfulRun)
                        .toList();

        int runCount = runs.size();
        int successCount = successfulRuns.size();
        int failureCount = runCount - successCount;

        return new BenchmarkStatistics(
                algorithm,
                runCount,
                successCount,
                failureCount,
                percentage(successCount, runCount),
                meanMoves(successfulRuns),
                medianMoves(successfulRuns),
                stdMoves(successfulRuns),
                bestMoves(successfulRuns),
                worstMoves(successfulRuns),
                meanRuntimeMs(successfulRuns)
        );
    }

    private boolean isSuccessfulRun(BenchmarkRun run) {
        return run.success() && run.moves() > 0;
    }

    private double percentage(int count, int total) {
        if (total == 0) {
            return 0.0;
        }

        return 100.0 * count / total;
    }

    private double meanMoves(List<BenchmarkRun> runs) {
        return runs.stream()
                .mapToInt(BenchmarkRun::moves)
                .average()
                .orElse(0.0);
    }

    private double medianMoves(List<BenchmarkRun> runs) {
        if (runs.isEmpty()) {
            return 0.0;
        }

        List<Integer> moves = runs.stream()
                .map(BenchmarkRun::moves)
                .sorted()
                .toList();

        int middle = moves.size() / 2;

        if (moves.size() % 2 == 1) {
            return moves.get(middle);
        }

        return (moves.get(middle - 1) + moves.get(middle)) / 2.0;
    }

    private double stdMoves(List<BenchmarkRun> runs) {
        if (runs.size() <= 1) {
            return 0.0;
        }

        double mean = meanMoves(runs);

        double variance = runs.stream()
                .mapToDouble(run -> Math.pow(run.moves() - mean, 2))
                .sum() / (runs.size() - 1);

        return Math.sqrt(variance);
    }

    private int bestMoves(List<BenchmarkRun> runs) {
        return runs.stream()
                .mapToInt(BenchmarkRun::moves)
                .min()
                .orElse(0);
    }

    private int worstMoves(List<BenchmarkRun> runs) {
        return runs.stream()
                .mapToInt(BenchmarkRun::moves)
                .max()
                .orElse(0);
    }

    private double meanRuntimeMs(List<BenchmarkRun> runs) {
        return runs.stream()
                .mapToLong(BenchmarkRun::runtimeMs)
                .average()
                .orElse(0.0);
    }
}