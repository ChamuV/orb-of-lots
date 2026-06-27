package student.benchmark;

import lombok.Getter;
import lombok.Setter;

/**
 * Stores benchmark data for a single algorithm run.
 */
@Getter
public class BenchmarkResult {

    private final String algorithmName;

    @Setter
    private int moves;

    @Setter
    private long runtimeUs;

    @Setter
    private long seed;

    @Setter
    private boolean success = true;

    public BenchmarkResult(String algorithmName) {
        this.algorithmName = algorithmName;
    }
}