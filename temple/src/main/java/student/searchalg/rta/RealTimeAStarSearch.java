package student.searchalg.rta;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-Time A* (RTA*) exploration strategy.
 *
 * <p>RTA* is a forward-only, real-time heuristic search algorithm. Unlike the
 * DFS-based strategies in this project, it never backtracks — it always moves
 * to one of its immediate neighbours and never retraces a step to undo a
 * decision. This makes it a genuine online search algorithm: the agent commits
 * to each move before the full consequences are known.
 *
 * <h2>Algorithm</h2>
 * <p>At each position, RTA* does three things:
 * <ol>
 *   <li>Compute {@code f(n) = 1 + h(n)} for every immediate neighbour, where
 *       {@code h(n)} is the best known heuristic estimate for node {@code n}
 *       (initialised from the game's distance-to-target and updated by
 *       experience).</li>
 *   <li>Update {@code h(current)} to the minimum {@code f} value across all
 *       neighbours. This raises the heuristic of the current node when the
 *       agent is in a dead end or a poor region, making the node less
 *       attractive on future visits and preventing infinite loops.</li>
 *   <li>Move to the neighbour with the lowest {@code f} value.</li>
 * </ol>
 *
 * <h2>Termination guarantee</h2>
 * <p>Because heuristics only ever increase, the agent cannot cycle forever:
 * every node it keeps revisiting becomes progressively less attractive until
 * another path dominates. On a connected graph with an admissible heuristic
 * the algorithm is guaranteed to reach the goal.
 *
 * <h2>Trade-offs vs other algorithms in this project</h2>
 * <ul>
 *   <li>Compared with {@code GreedyDFS}: RTA* never backtracks (fewer wasted
 *       moves on good maps) but may revisit nodes (more moves on adversarial
 *       maps). GreedyDFS guarantees each node is visited at most once.</li>
 *   <li>Compared with {@code AdaptiveHeuristicSearch}: that algorithm grafts
 *       an LRTA*-style update onto a DFS that still backtracks physically.
 *       RTA* is the correct home for the heuristic-learning idea.</li>
 *   <li>Compared with {@code ReplanningFrontierSearch}: frontier search
 *       maintains a global map and navigates optimally within it; RTA* uses
 *       only local information and one-step lookahead.</li>
 * </ul>
 */
public class RealTimeAStarSearch extends Algorithm {

    /**
     * Learned heuristic table. Initialised from the game's distance-to-target
     * values and updated upward as the agent explores.
     */
    private final Map<Long, Integer> heuristic = new HashMap<>();

    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            Collection<NodeStatus> neighbours = state.getNeighbours();

            seedHeuristics(neighbours);

            long current = state.getCurrentLocation();
            seedHeuristic(current, state.getDistanceToTarget());

            // Find the best neighbour and compute the update value in one pass.
            NodeStatus best         = null;
            int        bestF        = Integer.MAX_VALUE;
            int        secondBestF  = Integer.MAX_VALUE;

            for (NodeStatus nb : neighbours) {
                int f = 1 + heuristic.get(nb.nodeID());

                if (f < bestF) {
                    secondBestF = bestF;
                    bestF       = f;
                    best        = nb;
                } else if (f < secondBestF) {
                    secondBestF = f;
                }
            }

            // Update h(current) to the minimum f — but only ever upward.
            // Using bestF (not secondBestF) is the standard RTA* rule: set
            // h(s) = min_t [cost(s,t) + h(t)].  This ensures the current node
            // accurately reflects the best one-step lookahead cost from here,
            // preventing the agent from being drawn back immediately.
            if (bestF > heuristic.getOrDefault(current, 0)) {
                heuristic.put(current, bestF);
            }

            // Move to the best neighbour.
            state.moveTo(best.nodeID());
            recordMove();
        }
    }

    /**
     * Seeds the heuristic table for a neighbour if not yet seen.
     * Uses the game-provided distance-to-target as the initial estimate.
     */
    private void seedHeuristics(Collection<NodeStatus> neighbours) {
        for (NodeStatus nb : neighbours) {
            seedHeuristic(nb.nodeID(), nb.distanceToTarget());
        }
    }

    private void seedHeuristic(long nodeId, int gameDistance) {
        heuristic.putIfAbsent(nodeId, gameDistance);
    }
}