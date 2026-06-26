package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.Algorithm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for depth-first search exploration strategies.
 *
 * <p>This class implements the common depth-first search logic using an
 * explicit stack to record the traversal path for backtracking. Subclasses
 * define how neighbouring nodes are ordered, allowing different DFS
 * variants to share the same traversal algorithm while using different
 * neighbour selection heuristics.</p>
 */
public abstract class BaseDFS extends Algorithm {

    protected BaseDFS() {
        super();
    }

    BaseDFS(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    @Override
    protected void runSearch(ExplorationState state) {
        // Nodes that have already been explored.
        Set<Long> visited = new HashSet<>();

        // Path used to backtrack when a dead end is reached.
        Deque<Long> returnStack = new ArrayDeque<>();

        while (state.getDistanceToTarget() != 0) {
            long current = state.getCurrentLocation();
            visited.add(current);

            // Select the highest-priority unexplored neighbour.
            NodeStatus next = null;
            for (NodeStatus nb : orderedNeighbours(state)) {
                if (!visited.contains(nb.nodeID())) {
                    next = nb;
                    break;
                }
            }

            if (next != null) {
                // Continue exploring along the selected branch.
                returnStack.push(current);
                state.moveTo(next.nodeID());
                recordMove();
            } else {
                // No unexplored neighbours remain, so backtrack.
                if (returnStack.isEmpty()) {
                    return;
                }

                state.moveTo(returnStack.pop());
                recordMove();
            }
        }
    }

    /**
     * Returns the neighbouring nodes in the order they should be explored.
     *
     * <p>The first unvisited neighbour in the returned list will be selected
     * as the next node to visit.</p>
     *
     * @param state the current exploration state
     * @return an ordered list of neighbouring nodes
     */
    protected abstract List<NodeStatus> orderedNeighbours(ExplorationState state);
}