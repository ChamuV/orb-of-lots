package student.searchalg.frontier;

import game.ExplorationState;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

public class GradientFrontierUtilitySearch extends BaseFrontierSearch {

    private final double lambda;

    public GradientFrontierUtilitySearch() {
        this(2.0);
    }

    public GradientFrontierUtilitySearch(double lambda) {
        if (lambda <= 0) {
            throw new IllegalArgumentException(
                    "lambda must be positive; got " + lambda);
        }

        this.lambda = lambda;
    }

    protected GradientFrontierUtilitySearch(
            double lambda,
            BenchmarkWriter<BenchmarkResult> benchmarkWriter) {

        super(benchmarkWriter);

        if (lambda <= 0) {
            throw new IllegalArgumentException(
                    "lambda must be positive; got " + lambda);
        }

        this.lambda = lambda;
    }

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int candidateOrbDist = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int currentOrbDist = distToOrb.getOrDefault(currentLocation, candidateOrbDist);

        double gradientGain = currentOrbDist - candidateOrbDist;

        return travel - lambda * gradientGain;
    }

    @Override
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        return true;
    }
}