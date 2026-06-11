package student.searchalg.bestfirst;

import game.NodeStatus;

import java.util.Comparator;

/**
 * Best-first search using the estimated distance to the Orb.
 */
public class BestFirstSearch extends BaseBestFirstSearch {

    @Override
    protected Comparator<NodeStatus> neighbourComparator() {
        return NodeStatus::compareTo;
    }
}