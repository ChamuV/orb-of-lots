package student.benchmark.csv;

/**
 * Header definition for per-run benchmark files.
 */
public class RunCsvHeader extends AbstractCsvHeader {

    @Override
    protected String[] columns() {
        return new String[]{
                "algorithm",
                "moves",
                "runtime_ms"
        };
    }
}