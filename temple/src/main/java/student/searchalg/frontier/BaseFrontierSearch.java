package student.searchalg.frontier;

import game.ExplorationState;
import game.NodeStatus;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.Algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Abstract base class for frontier-based exploration strategies.
 *
 * <p>Maintains an incremental model of the cavern as the explorer moves,
 * tracking visited nodes, the current frontier (unvisited nodes adjacent to
 * visited ones), and known distances to the Orb. At each step, the algorithm
 * selects the most promising frontier node by score and navigates to it along
 * the shortest known path.
 *
 * <p>Subclasses define the selection criterion by implementing
 * {@link #score(long, long, Map, Map)}. Subclasses that wish to replan
 * mid-navigation may override {@link #shouldReplan(ExplorationState, long)}.
 * The shared {@code orb + travel} scoring formula is available via
 * {@link #baseScore(long, Map, Map)}.
 */
public abstract class BaseFrontierSearch extends Algorithm {

    /** Creates an instance with the default CSV benchmark writer. */
    protected BaseFrontierSearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    protected BaseFrontierSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /** Adjacency list of the explored portion of the cavern. */
    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();

    /** Most recently observed distance to the Orb for each known node. */
    private final Map<Long, Integer> distanceToOrb = new HashMap<>();

    /** Nodes that have been physically visited by the explorer. */
    private final Set<Long> visited = new HashSet<>();

    /**
     * Nodes discovered but not yet visited — the current search frontier.
     * A node is added when first seen as a neighbour, and removed when visited.
     */
    private final Set<Long> frontier = new HashSet<>();

    /**
     * Explores the cavern by repeatedly selecting and navigating to the
     * highest-scoring frontier node until the Orb is found or no frontier
     * nodes remain.
     *
     * @param state the current exploration state
     */
    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            updateModel(state);

            Long target = chooseBestFrontierNode(state.getCurrentLocation());
            if (target == null) {
                return;
            }

            navigateTo(state, target);
        }
    }

    /**
     * Updates the internal cavern model from the current position.
     *
     * <p>Marks the current node as visited, records its distance to the Orb,
     * and adds any newly discovered neighbours to the graph and frontier.
     *
     * @param state the current exploration state
     */
    private void updateModel(ExplorationState state) {
        long here = state.getCurrentLocation();

        visited.add(here);
        frontier.remove(here);

        knownGraph.computeIfAbsent(here, k -> new HashSet<>());
        distanceToOrb.put(here, state.getDistanceToTarget());

        for (NodeStatus nb : state.getNeighbours()) {
            long nbId = nb.nodeID();

            knownGraph.computeIfAbsent(nbId, k -> new HashSet<>());
            knownGraph.get(here).add(nbId);
            knownGraph.get(nbId).add(here);

            distanceToOrb.put(nbId, nb.distanceToTarget());

            if (!visited.contains(nbId)) {
                frontier.add(nbId);
            }
        }
    }

    /**
     * Returns the frontier node with the lowest score, or {@code null} if
     * the frontier is empty.
     *
     * @param currentLocation the explorer's current node ID
     * @return the best frontier node, or {@code null} if none remain
     */
    private Long chooseBestFrontierNode(long currentLocation) {
        if (frontier.isEmpty()) {
            return null;
        }

        Map<Long, Integer> travelCost = bfsDistances(currentLocation);

        Long best = null;
        double bestScore = Double.MAX_VALUE;

        for (long candidate : frontier) {
            double s = score(candidate, currentLocation, travelCost, distanceToOrb);
            if (s < bestScore) {
                bestScore = s;
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Navigates the explorer toward {@code target} along the shortest known
     * path, updating the cavern model after each step.
     *
     * <p>Navigation stops early if the Orb is found en route, the target
     * becomes unreachable, or {@link #shouldReplan(ExplorationState, long)}
     * returns {@code true}.
     *
     * @param state  the current exploration state
     * @param target the node ID to navigate toward
     */
    private void navigateTo(ExplorationState state, long target) {
        while (state.getCurrentLocation() != target
                && state.getDistanceToTarget() != 0) {

            List<Long> path = shortestPath(state.getCurrentLocation(), target);
            if (path.size() < 2) {
                break;
            }

            state.moveTo(path.get(1));
            recordMove();

            updateModel(state);

            if (shouldReplan(state, target)) {
                break;
            }
        }
    }

    /**
     * Scores a frontier candidate node for selection.
     *
     * <p>Lower scores are preferred. Subclasses define the scoring criterion;
     * the shared base formula ({@code orb + travel}) is available via
     * {@link #baseScore(long, Map, Map)}.
     *
     * @param candidate       the frontier node being scored
     * @param currentLocation the explorer's current node ID
     * @param travelCost      BFS distances from the current location
     * @param distToOrb       known distances to the Orb for each node
     * @return the score for this candidate; lower is better
     */
    protected abstract double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb);

    /**
     * Returns the base utility score for a candidate frontier node.
     *
     * <p>Computed as {@code orbDistance + travelCost}. Subclasses may use
     * this as the foundation for more specialised scoring functions.
     *
     * @param candidate  the frontier node being scored
     * @param travelCost BFS distances from the current location
     * @param distToOrb  known distances to the Orb for each node
     * @return the sum of the candidate's orb distance and travel cost
     */
    protected double baseScore(
            long candidate,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + travel;
    }

    /**
     * Returns whether the current navigation target should be abandoned in
     * favour of re-selecting from the frontier.
     *
     * <p>Returns {@code false} by default; subclasses may override to enable
     * mid-navigation replanning.
     *
     * @param state         the current exploration state
     * @param currentTarget the node currently being navigated toward
     * @return {@code true} if the algorithm should replan immediately
     */
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        return false;
    }

    /**
     * Returns BFS distances from {@code start} to all reachable nodes in the
     * known graph.
     *
     * @param start the source node ID
     * @return map from node ID to hop count from {@code start}
     */
    protected Map<Long, Integer> bfsDistances(long start) {
        Map<Long, Integer> dist = new HashMap<>();
        Queue<Long> queue = new ArrayDeque<>();

        dist.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();
            int d = dist.get(cur);

            for (long nb : knownGraph.getOrDefault(cur, Collections.emptySet())) {
                if (!dist.containsKey(nb)) {
                    dist.put(nb, d + 1);
                    queue.add(nb);
                }
            }
        }

        return dist;
    }

    /**
     * Returns the shortest known path from {@code start} to {@code target}.
     *
     * @param start  the source node ID
     * @param target the destination node ID
     * @return ordered list of node IDs from {@code start} to {@code target},
     *         or an empty list if no path is currently known
     */
    protected List<Long> shortestPath(long start, long target) {
        if (start == target) {
            return List.of(start);
        }

        Queue<Long> queue = new ArrayDeque<>();
        Map<Long, Long> parent = new HashMap<>();
        Set<Long> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();

            for (long nb : knownGraph.getOrDefault(cur, Collections.emptySet())) {
                if (seen.contains(nb)) {
                    continue;
                }

                seen.add(nb);
                parent.put(nb, cur);

                if (nb == target) {
                    return reconstructPath(parent, start, target);
                }

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

    /** Returns the known distances to the Orb for each discovered node. */
    protected Map<Long, Integer> distanceToOrb() {
        return distanceToOrb;
    }

    /** Returns the current frontier set. */
    protected Set<Long> frontier() {
        return frontier;
    }

    /** Returns the set of physically visited nodes. */
    protected Set<Long> visited() {
        return visited;
    }

    /**
     * Returns an unmodifiable view of the known neighbours of {@code node}.
     *
     * @param node the node ID to query
     * @return unmodifiable set of known adjacent node IDs
     */
    protected Set<Long> knownNeighbours(long node) {
        return Collections.unmodifiableSet(
                knownGraph.getOrDefault(node, Collections.emptySet()));
    }
}