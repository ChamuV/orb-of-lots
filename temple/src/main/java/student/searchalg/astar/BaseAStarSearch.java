package student.searchalg.astar;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common code for A*-style exploration strategies.
 */
public abstract class BaseAStarSearch extends Algorithm {

    @Override
    protected void runSearch(ExplorationState state) {
        search(state, new HashSet<>(), 0);
    }

    private boolean search(
            ExplorationState state,
            Set<Long> visited,
            int costSoFar
    ) {
        if (state.getDistanceToTarget() == 0) {
            return true;
        }

        long currentLocation = state.getCurrentLocation();
        visited.add(currentLocation);

        for (NodeStatus neighbour : orderedNeighbours(state, costSoFar)) {
            long neighbourId = neighbour.nodeID();

            if (visited.contains(neighbourId)) {
                continue;
            }

            state.moveTo(neighbourId);
            recordMove();

            if (search(state, visited, costSoFar + 1)) {
                return true;
            }

            state.moveTo(currentLocation);
            recordMove();
        }

        return false;
    }

    private List<NodeStatus> orderedNeighbours(
            ExplorationState state,
            int costSoFar
    ) {
        List<NodeStatus> neighbours =
                new ArrayList<>(state.getNeighbours());

        neighbours.sort(
                Comparator.comparingInt(
                        neighbour -> priority(neighbour, costSoFar)
                )
        );

        return neighbours;
    }

    /**
     * Returns the priority assigned to a neighbouring tile.
     */
    protected abstract int priority(
            NodeStatus neighbour,
            int costSoFar
    );
}