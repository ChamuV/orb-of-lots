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
 * <p>This implementation builds an internal graph of the explored cavern and
 * expands nodes in breadth-first order. Because movement in the game is
 * physical rather than abstract, the explorer must repeatedly navigate across
 * the known graph to reach the next node selected by the BFS frontier.</p>
 *
 * <p>This strategy is included primarily as a baseline for comparison with
 * the depth-first and travel-aware exploration algorithms.</p>
 */
public class BreadthFirstSearch extends Algorithm {

    /** Adjacency list representing the explored portion of the cavern. */
    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();

    /** Nodes that have already been expanded by the BFS search. */
    private final Set<Long> visited = new HashSet<>();

    /** Queue of discovered nodes awaiting expansion. */
    private final Queue<Long> bfsQueue = new LinkedList<>();

    /**
     * Explores the cavern using a breadth-first search strategy.
     *
     * <p>The algorithm expands nodes in order of increasing graph distance
     * from the starting location. When the next frontier node is selected,
     * the explorer physically travels to that node using the shortest known
     * path before continuing the search.</p>
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        updateModel(state);
        bfsQueue.add(state.getCurrentLocation());

        while (state.getDistanceToTarget() != 0) {
            Long target = null;

            // Find the next queued node that has not already been expanded.
            while (!bfsQueue.isEmpty()) {
                long candidate = bfsQueue.peek();

                if (!visited.contains(candidate)) {
                    target = candidate;
                    break;
                }

                bfsQueue.poll();
            }

            if (target == null) {
                return;
            }

            List<Long> path = shortestPath(state.getCurrentLocation(), target);

            for (long step : path) {
                if (step == state.getCurrentLocation()) {
                    continue;
                }

                state.moveTo(step);
                recordMove();
                updateModel(state);

                if (state.getDistanceToTarget() == 0) {
                    return;
                }
            }

            bfsQueue.poll();

            for (long neighbour : knownGraph.getOrDefault(target, Set.of())) {
                if (!visited.contains(neighbour)) {
                    bfsQueue.add(neighbour);
                }
            }
        }
    }

    /**
     * Updates the internal graph with information visible from the current node.
     *
     * <p>Newly discovered nodes and connections are added to the explored graph,
     * allowing future navigation and breadth-first expansion.</p>
     *
     * @param state the current exploration state
     */
    private void updateModel(ExplorationState state) {
        long here = state.getCurrentLocation();
        visited.add(here);
        knownGraph.computeIfAbsent(here, key -> new HashSet<>());

        for (NodeStatus neighbour : state.getNeighbours()) {
            long neighbourId = neighbour.nodeID();

            knownGraph.computeIfAbsent(neighbourId, key -> new HashSet<>());
            knownGraph.get(here).add(neighbourId);
            knownGraph.get(neighbourId).add(here);
        }
    }

    /**
     * Computes the shortest known path between two explored nodes.
     *
     * <p>The search is performed over the currently known graph using a
     * standard breadth-first search.</p>
     *
     * @param start the starting node ID
     * @param target the destination node ID
     * @return the shortest known path from {@code start} to {@code target},
     *         or an empty list if no path is currently known
     */
    private List<Long> shortestPath(long start, long target) {
        if (start == target) {
            return List.of(start);
        }

        Queue<Long> queue = new LinkedList<>();
        Map<Long, Long> parent = new HashMap<>();
        Set<Long> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long current = queue.remove();

            for (long neighbour : knownGraph.getOrDefault(current, Set.of())) {
                if (seen.contains(neighbour)) {
                    continue;
                }

                seen.add(neighbour);
                parent.put(neighbour, current);

                if (neighbour == target) {
                    return reconstructPath(parent, start, target);
                }

                queue.add(neighbour);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Reconstructs a shortest path from the parent map produced by the graph search.
     *
     * @param parent parent relationship recorded during the search
     * @param start the starting node ID
     * @param target the destination node ID
     * @return the reconstructed path from {@code start} to {@code target}
     */
    private List<Long> reconstructPath(Map<Long, Long> parent, long start, long target) {
        List<Long> path = new ArrayList<>();
        long current = target;

        while (current != start) {
            path.add(current);
            current = parent.get(current);
        }

        path.add(start);
        Collections.reverse(path);
        return path;
    }
}