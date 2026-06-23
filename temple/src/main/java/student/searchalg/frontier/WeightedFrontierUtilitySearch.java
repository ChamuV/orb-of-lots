package student.searchalg.frontier;

import java.util.Map;

public class WeightedFrontierUtilitySearch extends BaseFrontierSearch {

    private static final double TRAVEL_WEIGHT = 2.0;

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + TRAVEL_WEIGHT * travel;
    }
}