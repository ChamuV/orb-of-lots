package student.benchmark;

import lombok.Getter;

@Getter
public class BenchmarkSummary {

    private final String algorithmName;

    private int runCount;

    private int totalMoves;

    private int bestMoves = Integer.MAX_VALUE;

    private int worstMoves = Integer.MIN_VALUE;

    private long totalRuntimeMs;

    public BenchmarkSummary(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public void addResult(BenchmarkResult result) {

        runCount++;

        totalMoves += result.getMoves();

        totalRuntimeMs += result.getRuntimeMs();

        bestMoves = Math.min(
                bestMoves,
                result.getMoves()
        );

        worstMoves = Math.max(
                worstMoves,
                result.getMoves()
        );
    }

    public double getAverageMoves() {
        return (double) totalMoves / runCount;
    }

    public double getAverageRuntimeMs() {
        return (double) totalRuntimeMs / runCount;
    }
}