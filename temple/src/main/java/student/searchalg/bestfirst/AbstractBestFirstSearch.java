package student.searchalg.bestfirst;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Common code for best-first exploration strategies.
 */
public abstract class AbstractBestFirstSearch extends Algorithm {

    @Override
    protected void runSearch(ExplorationState state) {
        search(state, new HashSet<>());
    }

    private boolean search(ExplorationState state, Set<Long> visited) {
        if (state.getDistanceToTarget() == 0) {
            return true;
        }

        long currentLocation = state.getCurrentLocation();
        visited.add(currentLocation);

        PriorityQueue<NodeStatus> frontier =
                new PriorityQueue<>(neighbourComparator());

        frontier.addAll(state.getNeighbours());

        while (!frontier.isEmpty()) {
            NodeStatus next = frontier.poll();
            long nextId = next.nodeID();

            if (visited.contains(nextId)) {
                continue;
            }

            state.moveTo(nextId);
            recordMove();

            if (search(state, visited)) {
                return true;
            }

            state.moveTo(currentLocation);
            recordMove();
        }

        return false;
    }

    protected abstract Comparator<NodeStatus> neighbourComparator();
}