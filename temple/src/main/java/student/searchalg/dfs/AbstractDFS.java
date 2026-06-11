package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.AbstractAlgorithm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common code for depth-first search strategies.
 */
public abstract class AbstractDFS extends AbstractAlgorithm {

    @Override
    public void findOrb(ExplorationState state) {
        startBenchmark();

        search(state, new HashSet<>());

        stopBenchmark();
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
            recordMove();

            if (search(state, visited)) {
                return true;
            }

            state.moveTo(currentLocation);
            recordMove();
        }

        return false;
    }

    protected abstract List<NodeStatus> orderedNeighbours(ExplorationState state);
}