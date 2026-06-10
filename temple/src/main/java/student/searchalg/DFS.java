package student.searchalg;

import game.ExplorationState;
import game.NodeStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic depth-first search exploration strategy.
 *
 * This algorithm systematically explores unvisited neighbouring tiles and
 * backtracks when it reaches a dead end. It prioritises correctness over speed:
 * if the Orb is reachable, DFS will eventually find it.
 */
public class DFS implements SearchAlgorithm {

    @Override
    public void findOrb(ExplorationState state) {
        search(state, new HashSet<>());
    }

    /**
     * Recursively explores the cavern from the current location.
     *
     * @param state   current exploration state
     * @param visited IDs of tiles already visited
     * @return true once the explorer is standing on the Orb
     */
    private boolean search(ExplorationState state, Set<Long> visited) {
        if (state.getDistanceToTarget() == 0) {
            return true;
        }

        long currentLocation = state.getCurrentLocation();
        visited.add(currentLocation);

        for (NodeStatus neighbour : state.getNeighbours()) {
            long neighbourId = neighbour.nodeID();

            if (visited.contains(neighbourId)) {
                continue;
            }

            state.moveTo(neighbourId);

            if (search(state, visited)) {
                return true;
            }

            state.moveTo(currentLocation);
        }

        return false;
    }
}