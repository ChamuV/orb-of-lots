package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.SearchAlgorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common code for depth-first search strategies.
 *
 * This class is not intended to be used directly, but provides shared
 * functionality for DFS implementations in this package.
 */
public abstract class AbstractDFS implements SearchAlgorithm {

    @Override
    public void findOrb(ExplorationState state) {
        search(state, new HashSet<>());
    }

    private boolean search(ExplorationState state, Set<Long> visited) {
        if (state.getDistanceToTarget() == 0) {
            return true;
        }

        long currentLocation = state.getCurrentLocation();
        visited.add(currentLocation);

        for (NodeStatus neighbour : orderedNeighbours(state)) {
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

    protected abstract List<NodeStatus> orderedNeighbours(ExplorationState state);
}