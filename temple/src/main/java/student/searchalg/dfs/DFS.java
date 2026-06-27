package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first search with no heuristic ordering.
 *
 * <p>Neighbours are explored in the order provided by the game engine.
 * Serves as a baseline for comparison with heuristic DFS variants.
 */
public class DFS extends BaseDFS {

    /** Creates an instance with the default CSV benchmark writer. */
    public DFS() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    DFS(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Returns neighbours in the order provided by the game engine. */
    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        return new ArrayList<>(state.getNeighbours());
    }
}