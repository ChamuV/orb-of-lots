package student.searchalg.idastar;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.Algorithm;

import java.util.ArrayList;
import java.util.Comparator;
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

    public IterativeDeepeningAStarSearch() {
        super();
    }

    IterativeDeepeningAStarSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Sentinel value returned when the Orb has been found. */
    private static final int FOUND = -1;

    /**
     * Explores the cavern using iterative deepening A* search.
     *
     * <p>The search begins with the current distance-to-target estimate as
     * the initial threshold. If an iteration fails to find the Orb, the
     * threshold is raised to the smallest pruned f-cost and the next
     * iteration begins. When an iteration fails, the recursive search has
     * already physically backtracked to the iteration start node.</p>
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        int threshold = state.getDistanceToTarget();

        while (true) {
            Set<Long> visited = new HashSet<>();

            int result = search(state, visited, 0, threshold);

            if (result == FOUND || result == Integer.MAX_VALUE) {
                return;
            }

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
     * <p>For unsuccessful branches, every move made during the search is
     * physically undone before this method returns, leaving the explorer at
     * the node from which this call was made. On success, the method returns
     * immediately so the explorer remains on the Orb.</p>
     *
     * @param state the current exploration state
     * @param visited nodes already on the current search path
     * @param g moves taken from the iteration start node
     * @param threshold current f-cost pruning threshold
     * @return {@link #FOUND} if the Orb is reached; otherwise the smallest
     *         pruned f-cost observed during this search
     */
    private int search(
            ExplorationState state,
            Set<Long> visited,
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
        neighbours.sort(Comparator.comparingInt(NodeStatus::distanceToTarget));

        int minPruned = Integer.MAX_VALUE;

        for (NodeStatus neighbour : neighbours) {
            long neighbourId = neighbour.nodeID();

            if (visited.contains(neighbourId)) {
                continue;
            }

            state.moveTo(neighbourId);
            recordMove();

            int result = search(state, visited, g + 1, threshold);

            if (result == FOUND) {
                return FOUND;
            }

            state.moveTo(current);
            recordMove();

            if (result < minPruned) {
                minPruned = result;
            }
        }

        visited.remove(current);
        return minPruned;
    }
}