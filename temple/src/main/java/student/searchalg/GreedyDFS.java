package student.searchalg;

import game.ExplorationState;
import game.NodeStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GreedyDFS implements SearchAlgorithm {

    @Override
    public void findOrb(ExplorationState state) {
        Set<Long> visited = new HashSet<>();
        search(state, visited);
    }

    private boolean search(ExplorationState state, Set<Long> visited) {
        if (state.getDistanceToTarget() == 0) {
            return true;
        }

        long currentLocation = state.getCurrentLocation();
        visited.add(currentLocation);

        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        Collections.sort(neighbours);

        for (NodeStatus neighbour : neighbours) {
            long neighbourId = neighbour.nodeID();

            if (!visited.contains(neighbourId)) {
                state.moveTo(neighbourId);

                if (search(state, visited)) {
                    return true;
                }

                state.moveTo(currentLocation);
            }
        }

        return false;
    }
}