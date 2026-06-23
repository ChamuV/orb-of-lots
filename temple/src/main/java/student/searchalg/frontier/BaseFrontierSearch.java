package student.searchalg.frontier;

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
 * Base class for frontier-based exploration strategies.
 *
 * <p>Maintains a partial model of the cavern built up incrementally as the
 * explorer moves: a known adjacency graph, per-node orb-distance estimates,
 * a visited set, and an open frontier of discovered-but-unvisited nodes.
 *
 * <p>Each outer iteration:
 * <ol>
 *   <li>Update the model from the current position.</li>
 *   <li>Run a single BFS from the current position to obtain travel cost to
 *       every known node in O(N) — not a separate BFS per candidate.</li>
 *   <li>Score every frontier node via {@link #score} and commit to the best.</li>
 *   <li>Navigate toward the target one step at a time, calling
 *       {@link #shouldReplan} after each step so subclasses can abort early
 *       and trigger re-evaluation.</li>
 * </ol>
 *
 * <p>Subclasses must implement {@link #score}.  They may also override
 * {@link #shouldReplan} to enable mid-route replanning (default: never replan).
 */
public abstract class BaseFrontierSearch extends Algorithm {

    // ------------------------------------------------------------------
    // Partial cavern model — built up as the explorer moves
    // ------------------------------------------------------------------

    /** Adjacency graph of all nodes observed so far. */
    private final Map<Long, Set<Long>> knownGraph    = new HashMap<>();

    /** Best known straight-line distance from each node to the Orb. */
    private final Map<Long, Integer>   distanceToOrb = new HashMap<>();

    /** Nodes whose immediate neighbours have all been observed. */
    private final Set<Long>            visited       = new HashSet<>();

    /** Nodes that have been seen but not yet visited. */
    private final Set<Long>            frontier      = new HashSet<>();

    // ------------------------------------------------------------------
    // Main search loop
    // ------------------------------------------------------------------

    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            updateModel(state);

            Long target = chooseBestFrontierNode(state.getCurrentLocation());
            if (target == null) {
                return; // frontier exhausted — should not happen on a solvable map
            }

            navigateTo(state, target);
        }
    }

    // ------------------------------------------------------------------
    // Model update
    // ------------------------------------------------------------------

    /**
     * Incorporates everything visible from the current position into the
     * partial cavern model.
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

    // ------------------------------------------------------------------
    // Target selection
    // ------------------------------------------------------------------

    /**
     * Picks the frontier node with the lowest {@link #score}, using travel
     * costs pre-computed by a single BFS from {@code currentLocation}.
     * Returns {@code null} if the frontier is empty.
     */
    private Long chooseBestFrontierNode(long currentLocation) {
        if (frontier.isEmpty()) {
            return null;
        }

        Map<Long, Integer> travelCost = bfsDistances(currentLocation);

        Long   best      = null;
        double bestScore = Double.MAX_VALUE;

        for (long candidate : frontier) {
            double s = score(candidate, travelCost, distanceToOrb);
            if (s < bestScore) {
                bestScore = s;
                best      = candidate;
            }
        }

        return best;
    }

    // ------------------------------------------------------------------
    // Navigation
    // ------------------------------------------------------------------

    /**
     * Moves the explorer toward {@code target} one step at a time, updating
     * the model at each position.  Breaks early if {@link #shouldReplan}
     * returns {@code true}, allowing the outer loop to pick a new target.
     */
    private void navigateTo(ExplorationState state, long target) {
        while (state.getCurrentLocation() != target
                && state.getDistanceToTarget() != 0) {

            updateModel(state);

            List<Long> path = shortestPath(state.getCurrentLocation(), target);
            if (path.size() < 2) {
                break; // already there, or target no longer reachable
            }

            state.moveTo(path.get(1));
            recordMove();

            if (shouldReplan(state, target)) {
                break; // outer loop re-scores the frontier
            }
        }
    }

    // ------------------------------------------------------------------
    // Subclass hooks
    // ------------------------------------------------------------------

    /**
     * Scores a frontier candidate.  Lower scores are preferred.
     *
     * @param candidate  the node being evaluated
     * @param travelCost hop-count distances from the current position to every
     *                   known node, computed by a single BFS
     * @param distToOrb  best known straight-line distance from each node to
     *                   the Orb
     */
    protected abstract double score(
            long candidate,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb);

    /**
     * Called after each physical step during navigation.  Returns {@code true}
     * to abort the current journey and re-evaluate the frontier immediately.
     *
     * <p>Default: always {@code false} — commit to the chosen target until
     * reached.  Override for mid-route replanning.
     *
     * @param state         current exploration state, after the step
     * @param currentTarget the target this navigation was heading toward
     */
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        return false;
    }

    // ------------------------------------------------------------------
    // Graph utilities — available to subclasses
    // ------------------------------------------------------------------

    /**
     * BFS from {@code start} over the known graph.
     * Returns the shortest hop-count distance to every reachable node.
     */
    protected Map<Long, Integer> bfsDistances(long start) {
        Map<Long, Integer> dist  = new HashMap<>();
        Queue<Long>        queue = new LinkedList<>();

        dist.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();
            int  d   = dist.get(cur);

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
     * Returns the shortest path from {@code start} to {@code target} as an
     * ordered list of node IDs (inclusive at both ends), or an empty list if
     * the target is unreachable within the known graph.
     */
    protected List<Long> shortestPath(long start, long target) {
        if (start == target) {
            return List.of(start);
        }

        Queue<Long>      queue  = new LinkedList<>();
        Map<Long, Long>  parent = new HashMap<>();
        Set<Long>        seen   = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long cur = queue.remove();

            for (long nb : knownGraph.getOrDefault(cur, Collections.emptySet())) {
                if (seen.contains(nb)) continue;
                seen.add(nb);
                parent.put(nb, cur);
                if (nb == target) return reconstructPath(parent, start, target);
                queue.add(nb);
            }
        }

        return Collections.emptyList();
    }

    private List<Long> reconstructPath(
            Map<Long, Long> parent, long start, long target) {

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

    // ------------------------------------------------------------------
    // Read-only model accessors for subclasses
    // ------------------------------------------------------------------

    /** Live orb-distance estimates for all observed nodes. */
    protected Map<Long, Integer> distanceToOrb() { return distanceToOrb; }

    /** The current open frontier (discovered but not yet visited). */
    protected Set<Long>          frontier()       { return frontier;      }

    /** Nodes whose neighbours have all been observed. */
    protected Set<Long>          visited()        { return visited;       }
}