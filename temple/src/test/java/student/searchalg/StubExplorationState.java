package student.searchalg;

import game.ExplorationState;
import game.NodeStatus;

import java.util.Collection;
import java.util.List;

/**
 * Minimal stub {@link ExplorationState} for unit testing search algorithms.
 *
 * <p>Models a two-node graph: the explorer starts at node 1 (distance 1)
 * with a single neighbour at node 2 (distance 0). Moving to node 2 finds
 * the Orb.
 */
public class StubExplorationState implements ExplorationState {

    private long current = 1L;

    @Override
    public long getCurrentLocation() {
        return current;
    }

    @Override
    public int getDistanceToTarget() {
        return current == 2L ? 0 : 1;
    }

    @Override
    public Collection<NodeStatus> getNeighbours() {
        if (current == 1L) {
            return List.of(new NodeStatus(2L, 0));
        }
        return List.of();
    }

    @Override
    public void moveTo(long id) {
        current = id;
    }
}