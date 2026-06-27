package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Depth-first search with an online-updated distance heuristic.
 *
 * <p>Initialises heuristic estimates from the game's distance-to-target
 * values, then raises them as exploration reveals that certain nodes lead
 * to less promising regions. This is a form of real-time heuristic search
 * applied within a DFS framework: nodes whose heuristic has been raised
 * by backtracking experience are deprioritised in later decisions.
 */
public class AdaptiveHeuristicSearch extends BaseDFS {

    /** Learned heuristic estimates, updated as the search progresses. */
    private final Map<Long, Integer> learnedHeuristic = new HashMap<>();

    /** Creates an instance with the default CSV benchmark writer. */
    public AdaptiveHeuristicSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    AdaptiveHeuristicSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Returns neighbours sorted by their current learned heuristic estimate.
     *
     * <p>Seeds any unseen nodes into the heuristic table, updates the current
     * node's estimate based on its best neighbour, then sorts neighbours by
     * ascending learned estimate.
     *
     * @param state the current exploration state
     * @return neighbours ordered from most to least promising
     */
    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        long currentLocation = state.getCurrentLocation();
        learnedHeuristic.putIfAbsent(currentLocation, state.getDistanceToTarget());

        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        for (NodeStatus neighbour : neighbours) {
            learnedHeuristic.putIfAbsent(
                    neighbour.nodeID(),
                    neighbour.distanceToTarget()
            );
        }

        updateCurrentHeuristic(currentLocation, neighbours);

        neighbours.sort(
                Comparator.comparingInt(
                        neighbour -> learnedHeuristic.getOrDefault(
                                neighbour.nodeID(),
                                neighbour.distanceToTarget()
                        )
                )
        );

        return neighbours;
    }

    /**
     * Raises the learned heuristic for the current node if a better estimate
     * is available from its neighbours.
     *
     * <p>The new estimate is {@code min(neighbourEstimate) + 1}. The stored
     * value only ever increases, so backtracking experience permanently
     * reduces a node's priority.
     *
     * @param currentLocation the current node ID
     * @param neighbours      visible neighbours from the current node
     */
    private void updateCurrentHeuristic(long currentLocation, List<NodeStatus> neighbours) {
        int bestNeighbourEstimate = Integer.MAX_VALUE;

        for (NodeStatus neighbour : neighbours) {
            int estimate = learnedHeuristic.getOrDefault(
                    neighbour.nodeID(),
                    neighbour.distanceToTarget()
            );
            bestNeighbourEstimate = Math.min(bestNeighbourEstimate, 1 + estimate);
        }

        if (bestNeighbourEstimate != Integer.MAX_VALUE) {
            int old = learnedHeuristic.getOrDefault(currentLocation, bestNeighbourEstimate);
            learnedHeuristic.put(currentLocation, Math.max(old, bestNeighbourEstimate));
        }
    }
}