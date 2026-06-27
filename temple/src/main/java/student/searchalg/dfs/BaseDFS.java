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
 * Abstract base class for depth-first search exploration strategies.
 *
 * <p>Implements DFS traversal using an explicit stack for backtracking.
 * Subclasses define the order in which neighbours are considered by
 * implementing {@link #orderedNeighbours(ExplorationState)}, allowing
 * different selection heuristics without duplicating traversal logic.
 */
public abstract class BaseDFS extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    protected BaseDFS() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    BaseDFS(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Explores the cavern using depth-first search with backtracking.
     *
     * <p>At each step, the first unvisited neighbour from
     * {@link #orderedNeighbours(ExplorationState)} is selected. If no
     * unvisited neighbours remain, the explorer backtracks along the
     * recorded path until an unexplored branch is found.
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        Set<Long> visited = new HashSet<>();
        Deque<Long> returnStack = new ArrayDeque<>();

        while (state.getDistanceToTarget() != 0) {
            long current = state.getCurrentLocation();
            visited.add(current);

            NodeStatus next = null;
            for (NodeStatus nb : orderedNeighbours(state)) {
                if (!visited.contains(nb.nodeID())) {
                    next = nb;
                    break;
                }
            }

            if (next != null) {
                returnStack.push(current);
                state.moveTo(next.nodeID());
                recordMove();
            } else {
                if (returnStack.isEmpty()) {
                    return;
                }
                state.moveTo(returnStack.pop());
                recordMove();
            }
        }
    }

    /**
     * Returns the neighbours of the current node in the order they should
     * be explored. The first unvisited entry is selected as the next step.
     *
     * @param state the current exploration state
     * @return neighbours in exploration priority order
     */
    protected abstract List<NodeStatus> orderedNeighbours(ExplorationState state);
}