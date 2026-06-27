package student.searchalg;

import game.ExplorationState;
import game.NodeStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurable stub {@link ExplorationState} for unit testing search algorithms.
 *
 * <p>Models an arbitrary undirected graph where node IDs, edges, and orb-distance
 * estimates are supplied at construction time. The explorer starts at node 0.
 *
 * <p>Usage:
 * <pre>
 *   // Linear graph: 0 -- 1 -- 2(orb)
 *   GraphExplorationState state = new GraphExplorationState.Builder()
 *       .orbAt(2)
 *       .edge(0, 1).edge(1, 2)
 *       .distance(0, 2).distance(1, 1).distance(2, 0)
 *       .build();
 * </pre>
 */
public class GraphExplorationState implements ExplorationState {

    private long current;
    private final long orbNode;
    private final Map<Long, List<Long>> adjacency;
    private final Map<Long, Integer> distToOrb;

    private GraphExplorationState(
            long start,
            long orbNode,
            Map<Long, List<Long>> adjacency,
            Map<Long, Integer> distToOrb) {
        this.current = start;
        this.orbNode = orbNode;
        this.adjacency = adjacency;
        this.distToOrb = distToOrb;
    }

    @Override
    public long getCurrentLocation() {
        return current;
    }

    @Override
    public int getDistanceToTarget() {
        return distToOrb.getOrDefault(current, Integer.MAX_VALUE);
    }

    @Override
    public Collection<NodeStatus> getNeighbours() {
        List<NodeStatus> result = new ArrayList<>();
        for (long nb : adjacency.getOrDefault(current, List.of())) {
            result.add(new NodeStatus(nb, distToOrb.getOrDefault(nb, Integer.MAX_VALUE)));
        }
        return result;
    }

    @Override
    public void moveTo(long id) {
        if (!adjacency.getOrDefault(current, List.of()).contains(id)) {
            throw new IllegalArgumentException(
                    "Cannot move from " + current + " to " + id + ": not adjacent");
        }
        current = id;
    }

    public long orbNode() {
        return orbNode;
    }

    // -------------------------------------------------------------------------

    public static class Builder {

        private long start = 0L;
        private long orb = -1L;
        private final Map<Long, List<Long>> adjacency = new HashMap<>();
        private final Map<Long, Integer> distToOrb = new HashMap<>();

        public Builder start(long nodeId) {
            this.start = nodeId;
            return this;
        }

        public Builder orbAt(long nodeId) {
            this.orb = nodeId;
            distToOrb.put(nodeId, 0);
            return this;
        }

        /** Adds a bidirectional edge between {@code a} and {@code b}. */
        public Builder edge(long a, long b) {
            adjacency.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
            adjacency.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
            return this;
        }

        /** Sets the orb-distance hint for a node. */
        public Builder distance(long nodeId, int dist) {
            distToOrb.put(nodeId, dist);
            return this;
        }

        public GraphExplorationState build() {
            if (orb < 0) {
                throw new IllegalStateException("orbAt() must be called before build()");
            }
            return new GraphExplorationState(start, orb, adjacency, distToOrb);
        }
    }
}