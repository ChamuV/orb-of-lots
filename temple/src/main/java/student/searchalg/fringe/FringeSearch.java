package student.searchalg.fringe;

import game.ExplorationState;
import student.searchalg.frontier.BaseFrontierSearch;

import java.util.Map;

/**
 * Threshold-gated frontier search loosely inspired by Fringe Search.
 *
 * <p>Scores candidates with {@code f = travelCost + orbDistance}, but
 * suppresses candidates whose {@code f} exceeds the current threshold.
 * The threshold starts at the lowest {@code f} seen and grows by one each
 * time no in-threshold candidate exists, ensuring all frontier nodes are
 * eventually considered while biasing early exploration toward low-cost paths.
 */
public class FringeSearch extends BaseFrontierSearch {

    private int threshold = Integer.MAX_VALUE;

    @Override
    protected double score(
            long candidate,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int orb    = distToOrb.getOrDefault(candidate,  Integer.MAX_VALUE / 2);

        if (travel == Integer.MAX_VALUE / 2 || orb == Integer.MAX_VALUE / 2) {
            return Double.MAX_VALUE;
        }

        int f = travel + orb;

        // Initialise threshold on first scored candidate.
        if (threshold == Integer.MAX_VALUE) {
            threshold = f;
        }

        if (f <= threshold) {
            return f; // within threshold — score normally
        }

        // Outside threshold — raise it and penalise so in-threshold nodes win.
        threshold = Math.min(threshold + 1, f);
        return f + threshold;
    }
}