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

        double mean = meanMoves(successfulRuns);
        double std = stdMoves(successfulRuns);

        return new BenchmarkStatistics(
                algorithm,
                runCount,
                successCount,
                failureCount,
                percentage(successCount, runCount),
                mean,
                medianMoves(successfulRuns),
                std,
                coefficientOfVariation(mean, std),
                percentileMoves(successfulRuns, 0.90),
                percentileMoves(successfulRuns, 0.95),
                bestMoves(successfulRuns),
                worstMoves(successfulRuns),
                meanRuntimeUs(successfulRuns)
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
        return percentileMoves(runs, 0.50);
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

    private double coefficientOfVariation(double mean, double std) {
        if (mean == 0.0) {
            return 0.0;
        }

        return std / mean;
    }

    private double percentileMoves(List<BenchmarkRun> runs, double percentile) {
        if (runs.isEmpty()) {
            return 0.0;
        }

        List<Integer> moves = runs.stream()
                .map(BenchmarkRun::moves)
                .sorted()
                .toList();

        double index = percentile * (moves.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return moves.get(lower);
        }

        double weight = index - lower;
        return moves.get(lower) * (1.0 - weight) + moves.get(upper) * weight;
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

    private double meanRuntimeUs(List<BenchmarkRun> runs) {
        return runs.stream()
                .mapToLong(BenchmarkRun::runtimeUs)
                .average()
                .orElse(0.0);
    }
}