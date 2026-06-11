package student.searchalg.astar;

import game.NodeStatus;

/**
 * A*-style exploration using path cost plus estimated distance to the Orb.
 */
public class AStarSearch extends BaseAStarSearch {

    @Override
    protected int priority(
            NodeStatus neighbour,
            int costSoFar
    ) {
        int nextCost = costSoFar + 1;
        int heuristic = neighbour.distanceToTarget();

        return nextCost + heuristic;
    }
}