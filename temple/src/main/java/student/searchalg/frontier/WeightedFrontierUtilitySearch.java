package student.searchalg.frontier;

/**
 * Weighted version of frontier utility search.
 *
 * This algorithm balances the apparent distance to the orb against the cost
 * of travelling to a candidate node through the known graph.
 */
public class WeightedFrontierUtilitySearch extends BaseFrontierSearch {

    private static final double ORB_WEIGHT = 1.0;
    private static final double TRAVEL_WEIGHT = 2.0;

    @Override
    protected double score(long currentLocation, long candidate) {
        int orbDistance = distanceToOrb.getOrDefault(candidate, Integer.MAX_VALUE);
        int travelCost = shortestPathLength(currentLocation, candidate);

        return ORB_WEIGHT * orbDistance + TRAVEL_WEIGHT * travelCost;
    }
}