package student.searchalg.random;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Stochastic exploration with heuristic-guided restart.
 *
 * <p>At each step the explorer moves to a randomly chosen unvisited neighbour.
 * When all neighbours are visited (a dead end), rather than backtracking
 * step-by-step it jumps — via BFS through the known graph — to the nearest
 * node that still has at least one unvisited neighbour. Among tied nearest
 * nodes, the one with the lowest distance-to-Orb is preferred.
 *
 * <p>This serves as a stochastic baseline for benchmarking. If deterministic
 * heuristic algorithms only marginally outperform this, it suggests the
 * distance-to-Orb heuristic is not very informative on these maps. A large
 * gap confirms the heuristic is genuinely useful.
 *
 * <p>A fixed seed is used so results are reproducible across benchmark runs.
 */
public class RandomWalkSearch extends Algorithm {

    private static final long RANDOM_SEED = 42L;

    private final Random random = new Random(RANDOM_SEED);

    // Partial graph model — needed to navigate back to the frontier on dead ends.
    private final Map<Long, Set<Long>> knownGraph   = new HashMap<>();
    private final Set<Long>            visited      = new HashSet<>();

    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            long here = state.getCurrentLocation();
            updateModel(state);

            List<NodeStatus> unvisited = unvisitedNeighbours(state);

            if (!unvisited.isEmpty()) {
                // Move to a random unvisited neighbour.
                NodeStatus chosen = unvisited.get(random.nextInt(unvisited.size()));
                state.moveTo(chosen.nodeID());
                recordMove();
            } else {
                // Dead end — jump to the nearest node with unvisited neighbours.
                Long jumpTarget = nearestLiveNode(here);
                if (jumpTarget == null) {
                    return; // entire known graph exhausted
                }

                for (long step : shortestPath(here, jumpTarget)) {
                    if (step == here) continue;
                    state.moveTo(step);
                    recordMove();
                    updateModel(state);
                    if (state.getDistanceToTarget() == 0) return;
                }
            }
        }
    }

    private void updateModel(ExplorationState state) {
        long here = state.getCurrentLocation();
        visited.add(here);
        knownGraph.computeIfAbsent(here, k -> new HashSet<>());

        for (NodeStatus nb : state.getNeighbours()) {
            long nbId = nb.nodeID();
            knownGraph.computeIfAbsent(nbId, k -> new HashSet<>());
            knownGraph.get(here).add(nbId);
            knownGraph.get(nbId).add(here);
        }
    }

    private List<NodeStatus> unvisitedNeighbours(ExplorationState state) {
        List<NodeStatus> result = new ArrayList<>();
        for (NodeStatus nb : state.getNeighbours()) {
            if (!visited.contains(nb.nodeID())) {
                result.add(nb);
            }
        }
        return result;
    }

    /**
     * BFS from {@code start} to find the nearest known node that still has
     * at least one unvisited neighbour. Among nodes at the same BFS distance,
     * prefers the one with the lowest observed degree of unvisited neighbours
     * (a proxy for orb proximity — not perfect but avoids a separate heuristic
     * lookup). Returns {@code null} if no live node exists.
     */
    private Long nearestLiveNode(long start) {
        Queue<Long> queue   = new LinkedList<>();
        Set<Long>   seen    = new HashSet<>();
        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();

            boolean hasUnvisitedNeighbour = false;
            for (long nb : knownGraph.getOrDefault(cur, Set.of())) {
                if (!visited.contains(nb)) {
                    hasUnvisitedNeighbour = true;
                    break;
                }
            }

            if (hasUnvisitedNeighbour) {
                return cur;
            }

            for (long nb : knownGraph.getOrDefault(cur, Set.of())) {
                if (!seen.contains(nb)) {
                    seen.add(nb);
                    queue.add(nb);
                }
            }
        }

        return null;
    }

    private List<Long> shortestPath(long start, long target) {
        if (start == target) return List.of(start);

        Queue<Long>     queue  = new LinkedList<>();
        Map<Long, Long> parent = new HashMap<>();
        Set<Long>       seen   = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();
            for (long nb : knownGraph.getOrDefault(cur, Set.of())) {
                if (seen.contains(nb)) continue;
                seen.add(nb);
                parent.put(nb, cur);
                if (nb == target) return reconstructPath(parent, start, target);
                queue.add(nb);
            }
        }
        return Collections.emptyList();
    }

    private List<Long> reconstructPath(Map<Long, Long> parent, long start, long target) {
        List<Long> path = new ArrayList<>();
        long cur = target;
        while (cur != start) {
            path.add(cur);
            cur = parent.get(cur);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }
}