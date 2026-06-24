package student.benchmark.analysis;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes per-seed win counts between benchmarked algorithms.
 */
public class BenchmarkComparisonAnalyzer {

    /**
     * Counts how often each algorithm achieves the fewest moves for a seed.
     *
     * <p>Only successful runs with positive move counts are considered.
     *
     * @param runs loaded benchmark runs
     * @return win counts sorted from most wins to fewest wins
     */
    public List<AlgorithmWinCount> computeWinCounts(List<BenchmarkRun> runs) {
        Map<Long, List<BenchmarkRun>> runsBySeed = runs.stream()
                .filter(run -> run.success() && run.moves() > 0)
                .collect(Collectors.groupingBy(BenchmarkRun::seed));

        Map<String, Long> winsByAlgorithm = runsBySeed.values()
                .stream()
                .map(this::bestRunForSeed)
                .collect(Collectors.groupingBy(
                        BenchmarkRun::algorithm,
                        Collectors.counting()
                ));

        return winsByAlgorithm.entrySet()
                .stream()
                .map(entry -> new AlgorithmWinCount(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparingLong(AlgorithmWinCount::wins).reversed())
                .toList();
    }

    private BenchmarkRun bestRunForSeed(List<BenchmarkRun> runsForSeed) {
        return runsForSeed.stream()
                .min(Comparator.comparingInt(BenchmarkRun::moves))
                .orElseThrow();
    }
}