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
 * Iterative Deepening A* (IDA*) exploration strategy.
 *
 * <p>IDA* runs a cost-bounded depth-first search repeatedly. Each iteration
 * uses an f-cost threshold: a branch is pruned whenever
 * {@code f(n) = g(n) + h(n)} exceeds the threshold, where {@code g(n)} is
 * the number of moves made from the start of this iteration and {@code h(n)}
 * is the game's distance-to-target heuristic. If the Orb is not found, the
 * threshold is raised to the smallest f-value that was pruned, and the search
 * restarts from the current position.
 *
 * <h2>Key difference from AStarSearch in this project</h2>
 * <p>{@code AStarSearch} tracks recursion depth as a proxy for path cost.
 * IDA* tracks actual moves taken ({@code g}), making the cost accounting
 * correct. The pruning is therefore meaningful: branches where the optimistic
 * total cost already exceeds the budget are genuinely skipped.
 *
 * <h2>API compromise</h2>
 * <p>In offline IDA* a failed iteration is simply discarded and the search
 * restarts from the root at zero cost. Here the explorer must physically
 * retrace its path back to the starting position, which costs moves that are
 * counted in the final score. IDA* therefore performs best when the Orb is
 * found within the first one or two threshold levels. On large maps requiring
 * many restarts the retrace overhead can exceed the savings from pruning.
 */
public class IterativeDeepeningAStarSearch extends Algorithm {

    private static final int FOUND = -1;

    @Override
    protected void runSearch(ExplorationState state) {
        long startNode = state.getCurrentLocation();
        int  threshold = state.getDistanceToTarget();

        while (true) {
            // pathStack records the forward path taken this iteration so we
            // can retrace back to startNode if this iteration fails.
            Deque<Long> pathStack = new ArrayDeque<>();
            Set<Long>   visited   = new HashSet<>();

            int result = search(state, visited, pathStack, 0, threshold);

            if (result == FOUND) return;
            if (result == Integer.MAX_VALUE) return; // graph exhausted

            // Retrace back to startNode before raising the threshold.
            retrace(state, pathStack, startNode);

            threshold = result;
        }
    }

    /**
     * Recursive cost-bounded DFS.
     *
     * @param state     current exploration state
     * @param visited   nodes already on the current path (cycle prevention)
     * @param pathStack nodes traversed this iteration, for retracing
     * @param g         moves taken from the iteration start node
     * @param threshold f-cost pruning threshold
     * @return {@link #FOUND} if the Orb is reached; otherwise the minimum
     *         f-cost that caused pruning (used as the next threshold)
     */
    private int search(
            ExplorationState state,
            Set<Long>        visited,
            Deque<Long>      pathStack,
            int              g,
            int              threshold
    ) {
        if (state.getDistanceToTarget() == 0) return FOUND;

        long current = state.getCurrentLocation();
        int  f       = g + state.getDistanceToTarget();

        if (f > threshold) return f;

        visited.add(current);

        // Sort neighbours by f-value so the most promising branch is tried first.
        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        neighbours.sort((a, b) -> Integer.compare(
                (g + 1) + a.distanceToTarget(),
                (g + 1) + b.distanceToTarget()
        ));

        int minPruned = Integer.MAX_VALUE;

        for (NodeStatus nb : neighbours) {
            long nbId = nb.nodeID();
            if (visited.contains(nbId)) continue;

            state.moveTo(nbId);
            recordMove();
            pathStack.push(current);

            int result = search(state, visited, pathStack, g + 1, threshold);

            if (result == FOUND) return FOUND;

            // Backtrack physically.
            state.moveTo(current);
            recordMove();
            pathStack.pop();

            if (result < minPruned) minPruned = result;
        }

        visited.remove(current);
        return minPruned;
    }

    /**
     * Physically moves the explorer back to {@code startNode} by following
     * the recorded path in reverse.
     */
    private void retrace(
            ExplorationState state,
            Deque<Long>      pathStack,
            long             startNode
    ) {
        while (!pathStack.isEmpty()) {
            long step = pathStack.pop();
            state.moveTo(step);
            recordMove();
            if (step == startNode) break;
        }
    }
}