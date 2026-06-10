package student.benchmark;

/**
 * Stores statistics from a single search run.
 */
public class BenchmarkResult {

    private int moves;

    public void incrementMoves() {
        moves++;
    }

    public int getMoves() {
        return moves;
    }
}