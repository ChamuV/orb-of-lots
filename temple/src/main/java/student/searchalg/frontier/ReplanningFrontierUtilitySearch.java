package student.searchalg.frontier;

import game.ExplorationState;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

/**
 * Frontier search that replans when a significantly better target emerges.
 *
 * <p>Uses the same base utility score as {@link FrontierUtilitySearch} but
 * checks after each move whether any other frontier node scores at least
 * {@value #REPLAN_MARGIN} better than the current target. If so, navigation
 * is abandoned and the frontier is re-evaluated from the current position.
 */
public class ReplanningFrontierUtilitySearch extends BaseFrontierSearch {

    /**
     * Minimum score improvement required to trigger replanning.
     * Prevents replanning on negligible differences.
     */
    private static final double REPLAN_MARGIN = 1.0;

    /** Creates an instance with the default CSV benchmark writer. */
    public ReplanningFrontierUtilitySearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    ReplanningFrontierUtilitySearch(
            BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    /**
     * Scores a candidate using the base utility formula.
     *
     * @param candidate       the frontier node being scored
     * @param currentLocation the explorer's current node ID
     * @param travelCost      BFS distances from the current location
     * @param distToOrb       known distances to the Orb for each node
     * @return {@code orbDistance + travelCost} for the candidate
     */
    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        return baseScore(candidate, travelCost, distToOrb);
    }

    /**
     * Returns {@code true} if any frontier node scores at least
     * {@value #REPLAN_MARGIN} better than the current target.
     *
     * @param state         the current exploration state
     * @param currentTarget the node currently being navigated toward
     * @return {@code true} if a significantly better target exists
     */
    @Override
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        long here = state.getCurrentLocation();
        Map<Long, Integer> travelCost = bfsDistances(here);

        double currentScore = baseScore(currentTarget, travelCost, distanceToOrb());

        for (long candidate : frontier()) {
            if (candidate == currentTarget) {
                continue;
            }

            if (baseScore(candidate, travelCost, distanceToOrb()) < currentScore - REPLAN_MARGIN) {
                return true;
            }
        }

        return false;
    }
}