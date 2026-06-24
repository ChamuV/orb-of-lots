package student.searchalg.rta;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-Time A* exploration strategy.
 *
 * <p>This strategy performs online heuristic search using only the currently
 * visible neighbouring nodes. At each step, the explorer updates the heuristic
 * estimate for its current location, then moves to the neighbour with the
 * lowest estimated one-step cost.</p>
 *
 * <p>The heuristic table is initialised using the game's distance-to-target
 * estimates and is updated upward as the explorer learns that certain nodes
 * are less promising than they first appeared.</p>
 */
public class RealTimeAStarSearch extends Algorithm {

    /** Learned heuristic estimates indexed by node ID. */
    private final Map<Long, Integer> heuristic = new HashMap<>();

    /**
     * Explores the cavern using real-time A* search.
     *
     * <p>For each visible neighbour, the algorithm computes the one-step
     * estimate {@code f = 1 + h}. The current node's heuristic value is then
     * raised to the best such estimate, and the explorer moves immediately to
     * the neighbour with the lowest value.</p>
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            Collection<NodeStatus> neighbours = state.getNeighbours();

            seedHeuristics(neighbours);

            long current = state.getCurrentLocation();
            seedHeuristic(current, state.getDistanceToTarget());

            NodeStatus best = null;
            int bestF = Integer.MAX_VALUE;

            for (NodeStatus neighbour : neighbours) {
                int f = 1 + heuristic.get(neighbour.nodeID());

                if (f < bestF) {
                    bestF = f;
                    best = neighbour;
                }
            }

            if (bestF > heuristic.getOrDefault(current, 0)) {
                heuristic.put(current, bestF);
            }

            state.moveTo(best.nodeID());
            recordMove();
        }
    }

    /**
     * Adds initial heuristic estimates for visible neighbours.
     *
     * @param neighbours neighbouring nodes visible from the current position
     */
    private void seedHeuristics(Collection<NodeStatus> neighbours) {
        for (NodeStatus neighbour : neighbours) {
            seedHeuristic(neighbour.nodeID(), neighbour.distanceToTarget());
        }
    }

    /**
     * Adds an initial heuristic estimate for a node if it has not been seen before.
     *
     * @param nodeId the node ID to initialise
     * @param gameDistance the distance-to-target estimate provided by the game
     */
    private void seedHeuristic(long nodeId, int gameDistance) {
        heuristic.putIfAbsent(nodeId, gameDistance);
    }
}