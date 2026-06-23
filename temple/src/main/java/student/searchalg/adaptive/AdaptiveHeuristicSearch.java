package student.searchalg.adaptive;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.dfs.BaseDFS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LRTA*-inspired adaptive heuristic search.
 *
 * The algorithm begins with the game's distance-to-orb heuristic, but updates
 * remembered heuristic values as it explores. Nodes that appear less promising
 * after exploration become less attractive in later decisions.
 */
public class AdaptiveHeuristicSearch extends BaseDFS {

    private final Map<Long, Integer> learnedHeuristic = new HashMap<>();

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