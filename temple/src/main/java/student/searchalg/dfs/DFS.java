package student.searchalg.dfs;

import game.ExplorationState;
import game.NodeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic depth-first search exploration strategy.
 *
 * This algorithm systematically explores unvisited neighbouring tiles and
 * backtracks when it reaches a dead end. It prioritises correctness over speed:
 * if the Orb is reachable, DFS will eventually find it.
 */
public class DFS extends AbstractDFS {

    @Override
    protected List<NodeStatus> orderedNeighbours(ExplorationState state) {
        return new ArrayList<>(state.getNeighbours());
    }
}