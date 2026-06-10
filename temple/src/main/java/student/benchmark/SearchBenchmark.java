package student.benchmark;

/**
 * Utility class for collecting search performance statistics.
 */
public final class SearchBenchmark {

    private SearchBenchmark() {}

    public static void print(BenchmarkResult result) {
        System.out.println(
            "Moves taken: " + result.getMoves()
        );
    }
}