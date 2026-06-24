package student.benchmark.analysis;

import java.util.List;

/**
 * Prints benchmark analysis results in a readable console table.
 */
public class BenchmarkReportPrinter {

    public void printSummary(List<BenchmarkStatistics> statistics) {
        System.out.println("Benchmark Summary");
        System.out.println("=================");
        System.out.println();

        System.out.printf(
                "%-38s %5s %8s %8s %8s %8s %8s %8s %8s %8s %10s %12s%n",
                "Algorithm",
                "Runs",
                "Success",
                "Mean",
                "Median",
                "Std",
                "CV",
                "P90",
                "P95",
                "Best",
                "Worst",
                "Time(us)"
        );

        System.out.println("-".repeat(155));

        for (BenchmarkStatistics stats : statistics) {
            System.out.printf(
                    "%-38s %5d %7.1f%% %8.2f %8.2f %8.2f %8.3f %8.1f %8.1f %8d %10d %12.1f%n",
                    stats.algorithm(),
                    stats.runs(),
                    stats.successRate(),
                    stats.meanMoves(),
                    stats.medianMoves(),
                    stats.stdMoves(),
                    stats.coefficientOfVariation(),
                    stats.p90Moves(),
                    stats.p95Moves(),
                    stats.bestMoves(),
                    stats.worstMoves(),
                    stats.meanRuntimeUs()
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