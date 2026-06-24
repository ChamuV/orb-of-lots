package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first search strategy that prioritises nodes closer to the Orb.
 *
 * <p>Neighbouring nodes are ordered using the game's distance-to-target
 * heuristic before exploration. This typically guides the search towards
 * the Orb more quickly than standard DFS while retaining the same
 * depth-first traversal behaviour.</p>
 */
public class GreedyDFS extends BaseDFS {

    /**
     * Returns neighbouring nodes ordered by their estimated distance to the Orb.
     *
     * @param state the current exploration state
     * @return neighbouring nodes ordered from closest to furthest
     */
    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        neighbours.sort(NodeStatus::compareTo);
        return neighbours;
    }
}