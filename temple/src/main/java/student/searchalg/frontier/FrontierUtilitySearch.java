package student.searchalg.frontier;

/**
 * Frontier-based exploration strategy that balances orb distance with the
 * cost of travelling to a candidate node through the known graph.
 *
 * This algorithm avoids chasing a slightly better-looking frontier node if it
 * would require a large amount of backtracking to reach.
 */
public class FrontierUtilitySearch extends BaseFrontierSearch {

    @Override
    protected double score(long currentLocation, long candidate) {
        int orbDistance = distanceToOrb.getOrDefault(candidate, Integer.MAX_VALUE);
        int travelCost = shortestPathLength(currentLocation, candidate);

        return orbDistance + travelCost;
    }
}