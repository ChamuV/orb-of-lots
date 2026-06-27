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
 * <p>Repeatedly performs a cost-bounded depth-first search. Each iteration
 * prunes branches where {@code f = g + h} exceeds the current threshold,
 * where {@code g} is moves taken so far and {@code h} is the game's
 * distance-to-target estimate. When an iteration fails, the threshold is
 * raised to the smallest pruned f-cost and the search restarts.
 *
 * <p>Because exploration is physical, every pruned branch must be physically
 * backtracked before the next iteration begins, which makes this strategy
 * significantly more expensive per iteration than abstract IDA*.
 * Included as a heuristic search baseline.
 */
public class IterativeDeepeningAStarSearch extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    public IterativeDeepeningAStarSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    IterativeDeepeningAStarSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Sentinel value returned by {@link #search} when the Orb is found. */
    private static final int FOUND = -1;

    /**
     * Explores the cavern using iterative deepening A* search.
     *
     * <p>The initial threshold is the starting distance-to-target estimate.
     * Each failed iteration raises the threshold to the smallest pruned
     * f-cost. The recursive search physically backtracks all moves before
     * returning, so no explicit retrace is needed between iterations.
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
     * <p>Prunes branches where {@code f = g + h > threshold} and returns the
     * smallest pruned f-cost for use as the next threshold. Every move on an
     * unsuccessful branch is physically undone before returning, leaving the
     * explorer at the node from which this call was made. On success, returns
     * immediately with the explorer on the Orb.
     *
     * @param state     the current exploration state
     * @param visited   nodes on the current search path, to prevent revisiting
     * @param g         moves taken from the iteration start node
     * @param threshold current f-cost pruning threshold
     * @return {@link #FOUND} if the Orb is reached; otherwise the smallest
     *         pruned f-cost observed during this call
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