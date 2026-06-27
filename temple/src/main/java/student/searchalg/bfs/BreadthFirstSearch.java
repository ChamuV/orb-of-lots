package student.searchalg.bfs;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Breadth-first search exploration strategy.
 *
 * <p>Builds an incremental graph of the cavern as the explorer moves and
 * expands nodes in breadth-first order. Because exploration is physical,
 * the explorer must navigate across the known graph to reach each BFS
 * frontier node before expanding it.
 *
 * <p>Two separate sets distinguish nodes that have been physically visited
 * (used to build the graph) from nodes expanded by BFS (used to manage
 * search order). Conflating them would cause the algorithm to terminate
 * immediately, since the start node is physically visited before BFS
 * can select it for expansion.
 *
 * <p>Included as a baseline for comparison with heuristic and
 * frontier-based strategies.
 */
public class BreadthFirstSearch extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    public BreadthFirstSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    BreadthFirstSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Adjacency list of the explored portion of the cavern. */
    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();

    /**
     * Nodes physically visited by the explorer, used to build the graph model.
     * Kept separate from {@link #bfsExpanded} to avoid premature termination.
     */
    private final Set<Long> physicallyVisited = new HashSet<>();

    /**
     * Nodes already expanded by BFS. A node is expanded when popped from the
     * queue and its neighbours enqueued. Kept separate from
     * {@link #physicallyVisited} to avoid premature termination.
     */
    private final Set<Long> bfsExpanded = new HashSet<>();

    /** Nodes discovered but not yet BFS-expanded. */
    private final Queue<Long> bfsQueue = new ArrayDeque<>();

    /**
     * Explores the cavern using breadth-first search.
     *
     * <p>Nodes are expanded in order of increasing graph distance from the
     * start. For each selected node, the explorer physically navigates to it
     * along the shortest known path before expanding its neighbours.
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        updateModel(state);
        bfsQueue.add(state.getCurrentLocation());

        while (state.getDistanceToTarget() != 0) {
            Long target = null;

            while (!bfsQueue.isEmpty()) {
                long candidate = bfsQueue.peek();

                if (!bfsExpanded.contains(candidate)) {
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
            bfsExpanded.add(target);

            for (long neighbour : knownGraph.getOrDefault(target, Set.of())) {
                if (!bfsExpanded.contains(neighbour)) {
                    bfsQueue.add(neighbour);
                }
            }
        }
    }

    /**
     * Updates the internal graph from the current position.
     *
     * <p>Marks the node as physically visited and records all visible
     * neighbour edges. Does not mark the node as BFS-expanded.
     *
     * @param state the current exploration state
     */
    private void updateModel(ExplorationState state) {
        long here = state.getCurrentLocation();
        physicallyVisited.add(here);
        knownGraph.computeIfAbsent(here, key -> new HashSet<>());

        for (NodeStatus neighbour : state.getNeighbours()) {
            long neighbourId = neighbour.nodeID();

            knownGraph.computeIfAbsent(neighbourId, key -> new HashSet<>());
            knownGraph.get(here).add(neighbourId);
            knownGraph.get(neighbourId).add(here);
        }
    }

    /**
     * Returns the shortest known path from {@code start} to {@code target}.
     *
     * @param start  the source node ID
     * @param target the destination node ID
     * @return ordered list of node IDs, or an empty list if no path is known
     */
    private List<Long> shortestPath(long start, long target) {
        if (start == target) {
            return List.of(start);
        }

        Queue<Long> queue = new ArrayDeque<>();
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