package student.benchmark.analysis;

import java.util.List;

/**
 * Prints benchmark analysis results in a readable console table.
 */
public class BenchmarkReportPrinter {

    /**
     * Prints a ranked summary table of benchmark statistics.
     *
     * @param statistics per-algorithm benchmark statistics
     */
    public void printSummary(List<BenchmarkStatistics> statistics) {
        System.out.println("Benchmark Summary");
        System.out.println("=================");
        System.out.println();

        System.out.printf(
                "%-38s %5s %8s %8s %8s %8s %8s %8s%n",
                "Algorithm",
                "Runs",
                "Success",
                "Mean",
                "Median",
                "Std",
                "Best",
                "Worst"
        );

        System.out.println("-".repeat(100));

        for (BenchmarkStatistics stats : statistics) {
            System.out.printf(
                    "%-38s %5d %7.1f%% %8.2f %8.2f %8.2f %8d %8d%n",
                    stats.algorithm(),
                    stats.runs(),
                    stats.successRate(),
                    stats.meanMoves(),
                    stats.medianMoves(),
                    stats.stdMoves(),
                    stats.bestMoves(),
                    stats.worstMoves()
            );
        }

        System.out.println();
    }

    public void printWinCounts(List<AlgorithmWinCount> winCounts) {
        System.out.println("Win Counts");
        System.out.println("==========");
        System.out.println();

        System.out.printf("%-38s %8s%n", "Algorithm", "Wins");
        System.out.println("-".repeat(50));

        for (AlgorithmWinCount winCount : winCounts) {
            System.out.printf(
                    "%-38s %8d%n",
                    winCount.algorithm(),
                    winCount.wins()
            );
        }

        System.out.println();
    }
}