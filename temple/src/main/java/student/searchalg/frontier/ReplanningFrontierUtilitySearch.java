package student.searchalg.frontier;

import game.ExplorationState;

import java.util.Map;

/**
 * Frontier search with per-step replanning.
 *
 * <p>Uses the same scoring function as {@link FrontierUtilitySearch}
 * ({@code orbDistance + travelCost}), but re-evaluates the entire frontier
 * after every physical step rather than committing to a target until it is
 * reached.
 *
 * <p>After each step, a fresh BFS is run from the new position and all
 * frontier nodes are re-scored.  If any candidate beats the current target
 * by more than {@link #REPLAN_MARGIN}, the journey is abandoned and the
 * outer loop selects a new target immediately.  If the current target is
 * still best, it is simply re-selected and navigation continues seamlessly.
 *
 * <p>This matters because every step along a path reveals new neighbours.
 * A newly discovered node that is both close to the Orb and adjacent to the
 * current position can be exploited immediately rather than after completing
 * an increasingly suboptimal journey.
 */
public class ReplanningFrontierUtilitySearch extends BaseFrontierSearch {

    /**
     * Minimum score improvement required to abandon the current target.
     *
     * <p>Setting this to 0 switches on any improvement at all; higher values
     * reduce replanning churn on nearly-equivalent candidates.  A value of 1
     * means the new candidate must save at least one step net.
     */
    private static final double REPLAN_MARGIN = 1.0;

    @Override
    protected double score(
            long candidate,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb    = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + travel;
    }

    /**
     * After each step, recompute scores from the new position and replan if
     * any frontier node beats the current target by {@link #REPLAN_MARGIN}.
     */
    @Override
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        long here = state.getCurrentLocation();
        Map<Long, Integer> travelCost = bfsDistances(here);

        double currentScore = score(currentTarget, travelCost, distanceToOrb());

        for (long candidate : frontier()) {
            if (candidate == currentTarget) continue;
            if (score(candidate, travelCost, distanceToOrb()) < currentScore - REPLAN_MARGIN) {
                return true;
            }
        }

        return false;
    }
}