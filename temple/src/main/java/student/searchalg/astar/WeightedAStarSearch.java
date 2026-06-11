package student.searchalg.astar;

import game.NodeStatus;

/**
 * Weighted A* exploration strategy.
 *
 * Standard A* uses:
 *
 *     f(n) = g(n) + h(n)
 *
 * where:
 *     g(n) = path cost from the start node
 *     h(n) = estimated distance to the Orb
 *
 * Weighted A* places greater emphasis on the heuristic:
 *
 *     f(n) = g(n) + w * h(n)
 *
 * where:
 *     w > 1
 *
 * Larger values of w make the search behave more greedily by
 * prioritising nodes that appear closer to the Orb.
 */
public class WeightedAStarSearch extends BaseAStarSearch {

    private static final double HEURISTIC_WEIGHT = 1.5;

    @Override
    protected int priority(
            NodeStatus neighbour,
            int costSoFar
    ) {
        int nextCost = costSoFar + 1;
        int heuristic = neighbour.distanceToTarget();

        return (int) Math.round(
                nextCost + HEURISTIC_WEIGHT * heuristic
        );
    }
}