package student.benchmark.csv;

/**
 * Base class for CSV header definitions.
 */
public abstract class AbstractCsvHeader {

    /**
     * Returns the column names for this CSV type.
     */
    protected abstract String[] columns();

    /**
     * Returns the CSV header row.
     */
    public String value() {
        return String.join(",", columns());
    }
}