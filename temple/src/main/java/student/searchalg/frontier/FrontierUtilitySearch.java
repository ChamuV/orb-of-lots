package student.searchalg.frontier;

import java.util.Map;

/**
 * Frontier search that scores candidates as {@code orbDistance + travelCost}.
 *
 * <p>Balances proximity to the Orb against the cost of reaching the candidate
 * through the known graph, avoiding expensive detours to marginally
 * better-looking frontier nodes.
 */
public class FrontierUtilitySearch extends BaseFrontierSearch {

    @Override
    protected double score(
            long candidate,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb    = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + travel;
    }
}