package student.searchalg.fringe;

import game.ExplorationState;
import game.NodeStatus;
import student.searchalg.frontier.BaseFrontierSearch;

/**
 * Frontier-based approximation of Fringe Search.
 *
 * The algorithm evaluates frontier nodes using f = travelCost + distanceToOrb,
 * but only considers nodes within the current fringe limit. If no frontier node
 * is inside the current limit, the limit is raised to the next best available
 * f-value.
 */
public class FringeSearch extends BaseFrontierSearch {

    private int fringeLimit = Integer.MAX_VALUE;

    @Override
    protected double score(long currentLocation, long candidate) {
        int orbDistance = distanceToOrb.getOrDefault(candidate, Integer.MAX_VALUE);
        int travelCost = shortestPathLength(currentLocation, candidate);

        if (travelCost == Integer.MAX_VALUE || orbDistance == Integer.MAX_VALUE) {
            return Double.MAX_VALUE;
        }

        int fringeScore = travelCost + orbDistance;

        if (fringeLimit == Integer.MAX_VALUE) {
            fringeLimit = fringeScore;
        }

        if (fringeScore <= fringeLimit) {
            return fringeScore;
        }

        fringeLimit = Math.min(fringeLimit + 1, fringeScore);
        return fringeScore + fringeLimit;
    }
}