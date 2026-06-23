package student.searchalg.frontier;

import game.ExplorationState;

import java.util.Map;

public class ReplanningFrontierUtilitySearch extends BaseFrontierSearch {

    private static final double REPLAN_MARGIN = 1.0;

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + travel;
    }

    @Override
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        long here = state.getCurrentLocation();
        Map<Long, Integer> travelCost = bfsDistances(here);

        double currentScore = score(
                currentTarget,
                here,
                travelCost,
                distanceToOrb());

        for (long candidate : frontier()) {
            if (candidate == currentTarget) {
                continue;
            }

            double candidateScore = score(
                    candidate,
                    here,
                    travelCost,
                    distanceToOrb());

            if (candidateScore < currentScore - REPLAN_MARGIN) {
                return true;
            }
        }

        return false;
    }
}