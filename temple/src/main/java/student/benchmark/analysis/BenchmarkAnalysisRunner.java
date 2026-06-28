package student.benchmark.analysis;

import java.util.List;

/**
 * Entry point for loading and analysing benchmark results.
 */
public class BenchmarkAnalysisRunner {

    /**
     * Loads all benchmark runs from CSV files, computes per-algorithm
     * statistics and per-seed win counts, then prints both to standard output.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        BenchmarkLoader loader = new BenchmarkLoader();
        BenchmarkAnalyzer analyzer = new BenchmarkAnalyzer();
        BenchmarkComparisonAnalyzer comparisonAnalyzer =
                new BenchmarkComparisonAnalyzer();
        BenchmarkReportPrinter printer = new BenchmarkReportPrinter();

        List<BenchmarkRun> runs = loader.loadRuns();
        List<BenchmarkStatistics> statistics = analyzer.analyze(runs);
        List<AlgorithmWinCount> winCounts =
                comparisonAnalyzer.computeWinCounts(runs);

        System.out.println("Loaded " + runs.size() + " benchmark runs.");
        System.out.println();

        printer.printSummary(statistics);
        printer.printWinCounts(winCounts);
    }
}