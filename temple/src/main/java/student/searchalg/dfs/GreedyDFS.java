package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first search strategy that prioritises neighbours estimated to be
 * closer to the Orb.
 *
 * The algorithm keeps the correctness guarantee of DFS, while using the
 * distance-to-target heuristic to usually reduce unnecessary exploration.
 */
public class GreedyDFS extends AbstractDFS {

    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        List<NodeStatus> neighbours = new ArrayList<>(state.getNeighbours());
        neighbours.sort(NodeStatus::compareTo);
        return neighbours;
    }
}