package student.searchalg.rta;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.Algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-Time A* exploration strategy.
 *
 * <p>At each step, selects the neighbour with the lowest one-step cost
 * estimate {@code f = 1 + h(neighbour)}, then raises the current node's
 * heuristic to that value if it is higher. This upward update discourages
 * revisiting nodes that have proven less promising than they appeared.
 *
 * <p>Heuristic values are seeded from the game's distance-to-target
 * estimates on first observation and only ever increase thereafter.
 */
public class RealTimeAStarSearch extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    public RealTimeAStarSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    RealTimeAStarSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Learned heuristic estimates, seeded from game distances and updated upward. */
    private final Map<Long, Integer> heuristic = new HashMap<>();

    /**
     * Explores the cavern using real-time A* search.
     *
     * <p>Each iteration seeds heuristics for visible neighbours, selects the
     * neighbour with the lowest {@code f = 1 + h} value, raises the current
     * node's heuristic if the best {@code f} exceeds it, then moves.
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            Collection<NodeStatus> neighbours = state.getNeighbours();

            seedHeuristics(neighbours);

            long current = state.getCurrentLocation();
            seedHeuristic(current, state.getDistanceToTarget());

            NodeStatus best = null;
            int bestF = Integer.MAX_VALUE;

            for (NodeStatus neighbour : neighbours) {
                int f = 1 + heuristic.get(neighbour.nodeID());

                if (f < bestF) {
                    bestF = f;
                    best = neighbour;
                }
            }

            if (best == null) {
                return;
            }

            if (bestF > heuristic.getOrDefault(current, 0)) {
                heuristic.put(current, bestF);
            }

            state.moveTo(best.nodeID());
            recordMove();
        }
    }

    /** Seeds initial heuristic estimates for all visible neighbours. */
    private void seedHeuristics(Collection<NodeStatus> neighbours) {
        for (NodeStatus neighbour : neighbours) {
            seedHeuristic(neighbour.nodeID(), neighbour.distanceToTarget());
        }
    }

    /** Records {@code gameDistance} as the heuristic for {@code nodeId} if not yet seen. */
    private void seedHeuristic(long nodeId, int gameDistance) {
        heuristic.putIfAbsent(nodeId, gameDistance);
    }
}