package student.searchalg.bfs;

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
import java.util.Set;

/**
 * Breadth-first search exploration strategy.
 *
 * <p>Maintains a queue of discovered-but-unvisited nodes ordered by the number
 * of hops from the start, and always expands the shallowest node next. In an
 * offline graph search this guarantees the shortest path to the goal.
 *
 * <p><b>Why this performs poorly in practice.</b> Standard BFS assumes node
 * expansion is free. Here the explorer must physically walk to each node before
 * expanding it, and those travel moves count toward the final score. Expanding
 * the shallowest node often requires crossing the entire known graph, producing
 * large travel overhead. On a map of N nodes BFS makes O(N²) physical moves in
 * the worst case — quadratic in map size — compared with O(N) for DFS-based
 * strategies.
 *
 * <p>This implementation is included as an experimental lower bound to
 * demonstrate that algorithmic optimality in the abstract does not translate
 * to optimality under physical movement constraints. The contrast with
 * {@code FrontierUtilitySearch} — which also selects targets globally but
 * scores them by travel cost — illustrates why travel-cost-aware target
 * selection is essential.
 */
public class BreadthFirstSearch extends Algorithm {

    private final Map<Long, Set<Long>> knownGraph  = new HashMap<>();
    private final Set<Long>            visited     = new HashSet<>();
    private final Queue<Long>          bfsQueue    = new LinkedList<>();

    @Override
    protected void runSearch(ExplorationState state) {
        updateModel(state);
        bfsQueue.add(state.getCurrentLocation());

        while (state.getDistanceToTarget() != 0) {

            // Find the next queued node that hasn't been visited yet.
            Long target = null;
            while (!bfsQueue.isEmpty()) {
                long candidate = bfsQueue.peek();
                if (!visited.contains(candidate)) {
                    target = candidate;
                    break;
                }
                bfsQueue.poll();
            }

            if (target == null) return; // queue exhausted

            // Physically navigate to the target.
            List<Long> path = shortestPath(state.getCurrentLocation(), target);
            for (long step : path) {
                if (step == state.getCurrentLocation()) continue;
                state.moveTo(step);
                recordMove();
                updateModel(state);
                if (state.getDistanceToTarget() == 0) return;
            }

            // Mark visited and enqueue newly discovered neighbours.
            bfsQueue.poll(); // remove the target we just reached
            for (long nb : knownGraph.getOrDefault(target, Set.of())) {
                if (!visited.contains(nb)) {
                    bfsQueue.add(nb);
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
        while (cur != start) { path.add(cur); cur = parent.get(cur); }
        path.add(start);
        Collections.reverse(path);
        return path;
    }
}