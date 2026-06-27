package student.searchalg.frontier;

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

public abstract class BaseFrontierSearch extends Algorithm {

    protected BaseFrontierSearch() {
        super();
    }

    protected BaseFrontierSearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    private final Map<Long, Set<Long>> knownGraph = new HashMap<>();
    private final Map<Long, Integer> distanceToOrb = new HashMap<>();
    private final Set<Long> visited = new HashSet<>();
    private final Set<Long> frontier = new HashSet<>();

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

    protected abstract double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb);

    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        return false;
    }

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

    protected Map<Long, Integer> distanceToOrb() {
        return distanceToOrb;
    }

    protected Set<Long> frontier() {
        return frontier;
    }

    protected Set<Long> visited() {
        return visited;
    }

    protected Set<Long> knownNeighbours(long node) {
        return Collections.unmodifiableSet(
            knownGraph.getOrDefault(node, Collections.emptySet()));
    }
}