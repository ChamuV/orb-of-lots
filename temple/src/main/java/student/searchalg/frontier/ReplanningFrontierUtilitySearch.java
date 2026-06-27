package student.searchalg.frontier;

import game.ExplorationState;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

public class ReplanningFrontierUtilitySearch extends BaseFrontierSearch {

    private static final double REPLAN_MARGIN = 1.0;

    public ReplanningFrontierUtilitySearch() {
        super();
    }

    ReplanningFrontierUtilitySearch(
            BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        return baseScore(candidate, travelCost, distToOrb);
    }

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