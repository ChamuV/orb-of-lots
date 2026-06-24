package student.benchmark.csv;

/**
 * Header definition for aggregate benchmark summary files.
 */
public class SummaryCsvHeader extends AbstractCsvHeader {

    @Override
    protected String[] columns() {
        return new String[]{
                "algorithm",
                "runs",
                "avg_moves",
                "best_moves",
                "worst_moves",
                "avg_runtime_us"
        };
    }
}