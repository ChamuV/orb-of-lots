package student.searchalg.frontier;

import game.ExplorationState;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

/**
 * Frontier search that greedily follows the steepest orb-distance gradient.
 *
 * <p>Scores each frontier candidate by how much orb-distance it saves relative
 * to travel cost, weighted by {@code lambda}:
 * <pre>
 *     score = travelCost - lambda * (currentOrbDistance - candidateOrbDistance)
 * </pre>
 *
 * <p>Higher {@code lambda} values prioritise large gradient gains over short
 * travel distances. The frontier is re-evaluated after every move, making this
 * algorithm fully greedy.
 */
public class GradientFrontierUtilitySearch extends BaseFrontierSearch {

    /** Weight applied to the orb-distance gradient. Must be positive. */
    private final double lambda;

    private static double requirePositiveLambda(double lambda) {
        if (lambda <= 0) {
            throw new IllegalArgumentException(
                    "lambda must be positive; got " + lambda);
        }
        return lambda;
    }

    /** Creates an instance with the default gradient weight ({@code lambda = 2.0}). */
    public GradientFrontierUtilitySearch() {
        this(2.0);
    }

    /**
     * Creates an instance with the given gradient weight.
     *
     * @param lambda gradient weight; must be positive
     * @throws IllegalArgumentException if {@code lambda} is not positive
     */
    public GradientFrontierUtilitySearch(double lambda) {
        this.lambda = requirePositiveLambda(lambda);
    }

    /**
     * Creates an instance with the given gradient weight and benchmark writer.
     *
     * @param lambda          gradient weight; must be positive
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     * @throws IllegalArgumentException if {@code lambda} is not positive
     */
    protected GradientFrontierUtilitySearch(
            double lambda,
            BenchmarkWriter<BenchmarkResult> benchmarkWriter) {

        super(benchmarkWriter);
        this.lambda = requirePositiveLambda(lambda);
    }

    /**
     * Scores a candidate by travel cost minus a weighted orb-distance gradient.
     *
     * @param candidate       the frontier node being scored
     * @param currentLocation the explorer's current node ID
     * @param travelCost      BFS distances from the current location
     * @param distToOrb       known distances to the Orb for each node
     * @return the gradient-weighted utility score; lower is better
     */
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

    /** Always replans so the frontier is re-evaluated after every move. */
    @Override
    protected boolean shouldReplan(ExplorationState state, long currentTarget) {
        return true;
    }
}