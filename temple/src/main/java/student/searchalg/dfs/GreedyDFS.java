package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first search that prioritises neighbours closer to the Orb.
 *
 * <p>Neighbours are sorted by the game's distance-to-target estimate before
 * exploration, guiding the search toward the Orb faster than unordered DFS
 * while retaining the same backtracking behaviour.
 */
public class GreedyDFS extends BaseDFS {

    /** Creates an instance with the default CSV benchmark writer. */
    public GreedyDFS() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    GreedyDFS(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Returns neighbours sorted by ascending distance-to-target estimate.
     *
     * @param state the current exploration state
     * @return neighbours ordered from closest to furthest from the Orb
     */
    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        neighbours.sort(NodeStatus::compareTo);
        return neighbours;
    }
}