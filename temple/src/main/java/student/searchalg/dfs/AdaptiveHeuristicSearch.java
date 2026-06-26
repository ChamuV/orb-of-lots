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
 * Depth-first exploration strategy with an adaptive distance heuristic.
 *
 * <p>This strategy begins with the distance-to-orb estimates provided by the
 * game engine, then updates remembered heuristic values as the search
 * progresses. Nodes that lead towards less promising regions become less
 * attractive in later decisions.</p>
 */
public class AdaptiveHeuristicSearch extends BaseDFS {

    /** Learned heuristic estimates indexed by node ID. */
    private final Map<Long, Integer> learnedHeuristic = new HashMap<>();

    public AdaptiveHeuristicSearch() {
        super();
    }

    AdaptiveHeuristicSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Returns neighbouring nodes ordered by their learned heuristic estimate.
     *
     * <p>Before sorting, the current node and its neighbours are added to the
     * heuristic table if they have not been seen before. The current node's
     * estimate is then updated using the best known neighbouring estimate.</p>
     *
     * @param state the current exploration state
     * @return neighbouring nodes ordered from most to least promising
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
     * Updates the learned heuristic estimate for the current node.
     *
     * <p>The update is based on the best neighbouring estimate plus the cost
     * of moving to that neighbour. The stored value only increases, so nodes
     * that appear less useful after exploration are deprioritised later.</p>
     *
     * @param currentLocation the ID of the current node
     * @param neighbours the neighbouring nodes visible from the current node
     */
    private void updateCurrentHeuristic(long currentLocation, List<NodeStatus> neighbours) {
        int bestNeighbourEstimate = Integer.MAX_VALUE;

        for (NodeStatus neighbour : neighbours) {
            int neighbourEstimate = learnedHeuristic.getOrDefault(
                    neighbour.nodeID(),
                    neighbour.distanceToTarget()
            );

            bestNeighbourEstimate = Math.min(bestNeighbourEstimate, 1 + neighbourEstimate);
        }

        if (bestNeighbourEstimate != Integer.MAX_VALUE) {
            int oldEstimate = learnedHeuristic.getOrDefault(currentLocation, bestNeighbourEstimate);
            learnedHeuristic.put(currentLocation, Math.max(oldEstimate, bestNeighbourEstimate));
        }
    }
}