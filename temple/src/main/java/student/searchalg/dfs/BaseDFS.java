package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common code for depth-first exploration strategies.
 *
 * <p>Uses an explicit stack rather than recursion to avoid stack overflow
 * on large maps. Subclasses control neighbour ordering via
 * {@link #orderedNeighbours}, which determines which branch is explored first.
 */
public abstract class BaseDFS extends Algorithm {

    @Override
    protected void runSearch(ExplorationState state) {
        Set<Long>   visited     = new HashSet<>();
        Deque<Long> returnStack = new ArrayDeque<>();

        while (state.getDistanceToTarget() != 0) {
            long current = state.getCurrentLocation();
            visited.add(current);

            // Find the first unvisited neighbour in priority order.
            NodeStatus next = null;
            for (NodeStatus nb : orderedNeighbours(state)) {
                if (!visited.contains(nb.nodeID())) {
                    next = nb;
                    break;
                }
            }

            if (next != null) {
                // Advance to the chosen neighbour.
                returnStack.push(current);
                state.moveTo(next.nodeID());
                recordMove();
            } else {
                // Dead end — backtrack one step.
                if (returnStack.isEmpty()) return;
                state.moveTo(returnStack.pop());
                recordMove();
            }
        }
    }

    /**
     * Returns the neighbours of the current position in the order they
     * should be explored. The first unvisited neighbour in this list will
     * be chosen.
     */
    protected abstract List<NodeStatus> orderedNeighbours(ExplorationState state);
}