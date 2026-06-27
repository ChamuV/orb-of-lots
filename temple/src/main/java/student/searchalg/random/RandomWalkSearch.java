package student.searchalg.random;

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
import java.util.Random;
import java.util.Set;

/**
 * Randomised exploration strategy with frontier recovery.
 *
 * <p>At each step, moves to a randomly selected unvisited neighbour.
 * When no unvisited neighbours remain, navigates to the nearest known
 * node that still has unexplored neighbours and resumes from there.
 *
 * <p>A fixed random seed ensures benchmark results are reproducible.
 */
public class RandomWalkSearch extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    public RandomWalkSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    RandomWalkSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Fixed seed used to make random choices reproducible across benchmark runs. */
    private static final long RANDOM_SEED = 42L;

    /** Random number generator seeded for reproducibility. */
    private final Random random = new Random(RANDOM_SEED);

    /** Adjacency list of the explored portion of the cavern. */
    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();

    /** Nodes that have already been physically visited. */
    private final Set<Long> visited = new HashSet<>();

    /**
     * Explores the cavern using a randomised walk with frontier recovery.
     *
     * <p>Randomly selects an unvisited neighbour at each step. If the
     * explorer reaches a dead end, it navigates to the nearest live node
     * (a node with at least one unvisited neighbour) and resumes.
     * Returns when the Orb is found or no live nodes remain.
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
     * Updates the internal graph from the current position.
     *
     * <p>Marks the current node as visited and records all visible
     * neighbour edges in the known graph.
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
     * Returns visible neighbours that have not yet been visited.
     *
     * @param state the current exploration state
     * @return unvisited neighbouring nodes
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
     * Returns the nearest known node with at least one unvisited neighbour,
     * or {@code null} if none exists.
     *
     * @param start the node ID from which to search
     * @return the nearest live node, or {@code null} if the frontier is exhausted
     */
    private Long nearestLiveNode(long start) {
        Queue<Long> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long current = queue.remove();

            for (long neighbour : knownGraph.getOrDefault(current, Set.of())) {
                if (!visited.contains(neighbour)) {
                    return current;
                }
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
     * Returns the shortest known path from {@code start} to {@code target},
     * or an empty list if no path is currently known.
     *
     * @param start  the source node ID
     * @param target the destination node ID
     * @return ordered list of node IDs from {@code start} to {@code target}
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