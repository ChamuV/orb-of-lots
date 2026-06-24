package student.searchalg.idastar;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Iterative Deepening A* exploration strategy.
 *
 * <p>This strategy repeatedly performs a cost-bounded depth-first search.
 * Each iteration uses an {@code f = g + h} threshold, where {@code g} is the
 * number of moves made during the current iteration and {@code h} is the
 * game's distance-to-target heuristic.</p>
 *
 * <p>This implementation is included as a heuristic search baseline. Because
 * the explorer must physically move and backtrack in the cavern, repeated
 * threshold increases can introduce substantial movement overhead.</p>
 */
public class IterativeDeepeningAStarSearch extends Algorithm {

    /** Sentinel value returned when the Orb has been found. */
    private static final int FOUND = -1;

    /**
     * Explores the cavern using iterative deepening A* search.
     *
     * <p>The search begins with the current distance-to-target estimate as
     * the initial threshold. If an iteration fails to find the Orb, the
     * threshold is raised to the smallest pruned f-cost and the explorer
     * retraces back to the iteration start before searching again.</p>
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        long startNode = state.getCurrentLocation();
        int threshold = state.getDistanceToTarget();

        while (true) {
            Deque<Long> pathStack = new ArrayDeque<>();
            Set<Long> visited = new HashSet<>();

            int result = search(state, visited, pathStack, 0, threshold);

            if (result == FOUND) {
                return;
            }

            if (result == Integer.MAX_VALUE) {
                return;
            }

            retrace(state, pathStack, startNode);
            threshold = result;
        }
    }

    /**
     * Performs a recursive cost-bounded depth-first search.
     *
     * <p>A branch is pruned when its estimated total cost exceeds the current
     * threshold. The method returns the smallest f-cost that was pruned, which
     * is then used as the threshold for the next iteration.</p>
     *
     * @param state the current exploration state
     * @param visited nodes already on the current search path
     * @param pathStack path travelled during the current iteration
     * @param g moves taken from the iteration start node
     * @param threshold current f-cost pruning threshold
     * @return {@link #FOUND} if the Orb is reached; otherwise the smallest
     *         pruned f-cost observed during this search
     */
    private int search(
            ExplorationState state,
            Set<Long> visited,
            Deque<Long> pathStack,
            int g,
            int threshold
    ) {
        if (state.getDistanceToTarget() == 0) {
            return FOUND;
        }

        long current = state.getCurrentLocation();
        int f = g + state.getDistanceToTarget();

        if (f > threshold) {
            return f;
        }

        visited.add(current);

        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        neighbours.sort((a, b) -> Integer.compare(
                (g + 1) + a.distanceToTarget(),
                (g + 1) + b.distanceToTarget()
        ));

        int minPruned = Integer.MAX_VALUE;

        for (NodeStatus neighbour : neighbours) {
            long neighbourId = neighbour.nodeID();

            if (visited.contains(neighbourId)) {
                continue;
            }

            state.moveTo(neighbourId);
            recordMove();
            pathStack.push(current);

            int result = search(state, visited, pathStack, g + 1, threshold);

            if (result == FOUND) {
                return FOUND;
            }

            state.moveTo(current);
            recordMove();
            pathStack.pop();

            if (result < minPruned) {
                minPruned = result;
            }
        }

        visited.remove(current);
        return minPruned;
    }

    /**
     * Moves the explorer back to the iteration start node.
     *
     * <p>The path travelled during the failed iteration is followed in reverse
     * so that the next threshold level begins from the same start location.</p>
     *
     * @param state the current exploration state
     * @param pathStack path travelled during the current iteration
     * @param startNode node ID where the current iteration began
     */
    private void retrace(
            ExplorationState state,
            Deque<Long> pathStack,
            long startNode
    ) {
        while (!pathStack.isEmpty()) {
            long step = pathStack.pop();

            state.moveTo(step);
            recordMove();

            if (step == startNode) {
                break;
            }
        }
    }
}