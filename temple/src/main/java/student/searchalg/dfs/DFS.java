package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard depth-first search exploration strategy.
 *
 * <p>This implementation explores neighbouring nodes in the order provided
 * by the game engine. It performs no additional heuristic ordering, making
 * it a baseline DFS strategy for comparison with more informed variants.</p>
 */
public class DFS extends BaseDFS {

    public DFS() {
        super();
    }

    DFS(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Returns neighbouring nodes in their original order.
     *
     * @param state the current exploration state
     * @return the neighbouring nodes without reordering
     */
    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        return new ArrayList<>(state.getNeighbours());
    }
}