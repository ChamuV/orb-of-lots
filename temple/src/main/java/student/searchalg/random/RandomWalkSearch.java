package student.searchalg.random;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.Algorithm;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Randomised exploration strategy with frontier recovery.
 *
 * <p>At each step, the explorer moves to a randomly selected unvisited
 * neighbour. When no unvisited neighbours remain, the explorer uses the
 * known graph to navigate to the nearest node that still has unexplored
 * neighbouring nodes.</p>
 *
 * <p>A fixed random seed is used so that benchmark results are reproducible.</p>
 */
public class RandomWalkSearch extends Algorithm {

    public RandomWalkSearch() {
        super();
    }

    RandomWalkSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Fixed seed used to make random choices reproducible. */
    private static final long RANDOM_SEED = 42L;

    /** Random number generator used to select among unvisited neighbours. */
    private final Random random = new Random(RANDOM_SEED);

    /** Adjacency list representing the explored portion of the cavern. */
    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();

    /** Nodes that have already been visited. */
    private final Set<Long> visited = new HashSet<>();

    /**
     * Explores the cavern using a randomised walk strategy.
     *
     * <p>The algorithm randomly chooses among unvisited neighbouring nodes.
     * If it reaches a dead end, it computes a shortest known path to the
     * nearest live frontier node and resumes random exploration from there.</p>
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            long here = state.getCurrentLocation();
            updateModel(state);

            List<NodeStatus> unvisited = unvisitedNeighbours(state);

            if (!unvisited.isEmpty()) {
                NodeStatus chosen = unvisited.get(random.nextInt(unvisited.size()));
                state.moveTo(chosen.nodeID());
                recordMove();
            } else {
                Long jumpTarget = nearestLiveNode(here);

                if (jumpTarget == null) {
                    return;
                }

                for (long step : shortestPath(here, jumpTarget)) {
                    if (step == here) {
                        continue;
                    }

                    state.moveTo(step);
                    recordMove();
                    updateModel(state);

                    if (state.getDistanceToTarget() == 0) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Updates the internal graph with information visible from the current node.
     *
     * <p>The current node is marked as visited, and all currently visible
     * neighbour relationships are added to the known graph.</p>
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
     * Returns currently visible neighbours that have not yet been visited.
     *
     * @param state the current exploration state
     * @return visible neighbouring nodes that have not been visited
     */
    private List<NodeStatus> unvisitedNeighbours(ExplorationState state) {
        List<NodeStatus> result = new ArrayList<>();

        for (NodeStatus neighbour : state.getNeighbours()) {
            if (!visited.contains(neighbour.nodeID())) {
                result.add(neighbour);
            }
        }

        return result;
    }

    /**
     * Finds the nearest known node with at least one unvisited neighbour.
     *
     * <p>The search is performed over the known graph. A node is considered
     * live if it has at least one adjacent node that has not yet been visited.</p>
     *
     * @param start the node ID from which to begin the search
     * @return the nearest live node, or {@code null} if none exists
     */
    private Long nearestLiveNode(long start) {
        Queue<Long> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long current = queue.remove();

            boolean hasUnvisitedNeighbour = false;

            for (long neighbour : knownGraph.getOrDefault(current, Set.of())) {
                if (!visited.contains(neighbour)) {
                    hasUnvisitedNeighbour = true;
                    break;
                }
            }

            if (hasUnvisitedNeighbour) {
                return current;
            }

            for (long neighbour : knownGraph.getOrDefault(current, Set.of())) {
                if (!seen.contains(neighbour)) {
                    seen.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return null;
    }

    /**
     * Computes the shortest known path between two explored nodes.
     *
     * <p>The path is computed over the currently known graph using
     * breadth-first search.</p>
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