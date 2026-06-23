package student.searchalg.frontier;

import student.searchalg.frontier.BaseFrontierSearch;

import java.util.Map;

public class FringeSearch extends BaseFrontierSearch {

    private int threshold = Integer.MAX_VALUE;

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        if (travel == Integer.MAX_VALUE / 2 || orb == Integer.MAX_VALUE / 2) {
            return Double.MAX_VALUE;
        }

        int f = travel + orb;

        if (threshold == Integer.MAX_VALUE) {
            threshold = f;
        }

        if (f <= threshold) {
            return f;
        }

        threshold = Math.min(threshold + 1, f);
        return f + threshold;
    }
}