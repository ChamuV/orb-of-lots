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
 * Common code for frontier-based exploration strategies.
 */
public abstract class BaseFrontierSearch extends Algorithm {

    protected final Map<Long, Set<Long>> knownGraph = new HashMap<>();
    protected final Map<Long, Integer> distanceToOrb = new HashMap<>();
    protected final Set<Long> visited = new HashSet<>();
    protected final Set<Long> frontier = new HashSet<>();

    @Override
    protected void runSearch(ExplorationState state) {
        while (state.getDistanceToTarget() != 0) {
            updateKnowledge(state);

            Long target = chooseFrontierNode(state);

            if (target == null) {
                return;
            }

            moveToTarget(state, target);
        }
    }

    private void updateKnowledge(ExplorationState state) {
        long currentLocation = state.getCurrentLocation();

        visited.add(currentLocation);
        frontier.remove(currentLocation);

        knownGraph.putIfAbsent(currentLocation, new HashSet<>());
        distanceToOrb.put(currentLocation, state.getDistanceToTarget());

        for (NodeStatus neighbour : state.getNeighbours()) {
            long neighbourId = neighbour.nodeID();

            knownGraph.putIfAbsent(neighbourId, new HashSet<>());
            knownGraph.get(currentLocation).add(neighbourId);
            knownGraph.get(neighbourId).add(currentLocation);

            distanceToOrb.put(neighbourId, neighbour.distanceToTarget());

            if (!visited.contains(neighbourId)) {
                frontier.add(neighbourId);
            }
        }
    }

    private Long chooseFrontierNode(ExplorationState state) {
        long currentLocation = state.getCurrentLocation();

        Long bestNode = null;
        double bestScore = Double.MAX_VALUE;

        for (Long candidate : frontier) {
            double candidateScore = score(currentLocation, candidate);

            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestNode = candidate;
            }
        }

        return bestNode;
    }

    private void moveToTarget(ExplorationState state, long target) {
        long currentLocation = state.getCurrentLocation();
        List<Long> path = shortestPath(currentLocation, target);

        for (int i = 1; i < path.size(); i++) {
            state.moveTo(path.get(i));
            recordMove();

            if (state.getDistanceToTarget() == 0) {
                return;
            }
        }
    }

    protected int shortestPathLength(long start, long target) {
        List<Long> path = shortestPath(start, target);

        if (path.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        return path.size() - 1;
    }

    private List<Long> shortestPath(long start, long target) {
        Queue<Long> queue = new LinkedList<>();
        Map<Long, Long> parent = new HashMap<>();
        Set<Long> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty()) {
            long current = queue.remove();

            if (current == target) {
                return reconstructPath(parent, start, target);
            }

            for (long neighbour : knownGraph.getOrDefault(current, Collections.emptySet())) {
                if (seen.contains(neighbour)) {
                    continue;
                }

                seen.add(neighbour);
                parent.put(neighbour, current);
                queue.add(neighbour);
            }
        }

        return Collections.emptyList();
    }

    private List<Long> reconstructPath(Map<Long, Long> parent, long start, long target) {
        List<Long> path = new ArrayList<>();
        long current = target;

        path.add(current);

        while (current != start) {
            current = parent.get(current);
            path.add(current);
        }

        Collections.reverse(path);
        return path;
    }

    protected abstract double score(long currentLocation, long candidate);
}