package student.benchmark;

import lombok.Getter;
import lombok.Setter;

/**
 * Stores benchmark data for a single algorithm run.
 */
@Getter
@Setter
public class BenchmarkResult {

    private final String algorithmName;

    private int moves;
    private long runtimeUs;
    private long seed;
    private boolean success = true;

    public BenchmarkResult(String algorithmName) {
        this.algorithmName = algorithmName;
    }
}