package student.searchalg.frontier;

import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

/**
 * Frontier search that biases selection toward high-density frontier regions.
 *
 * <p>Extends the base utility score with a coverage bonus: frontier nodes
 * adjacent to several other frontier nodes are treated as gateways into
 * unexplored regions and preferred during selection.
 *
 * <p>Scoring function:
 * <pre>
 *     score = orbDistance + travelCost - mu * frontierDensity
 * </pre>
 *
 * <p>Higher values of {@code mu} increase the coverage bias. A value of
 * {@code 0} reduces this algorithm to {@link FrontierUtilitySearch}.
 */
public class CoverageBiasedFrontierUtilitySearch extends BaseFrontierSearch {

    /** Weight applied to the frontier-density bonus. Must be non-negative. */
    private final double mu;

    private static double requireNonNegativeMu(double mu) {
        if (mu < 0) {
            throw new IllegalArgumentException("mu must be non-negative; got " + mu);
        }
        return mu;
    }

    /** Creates an instance with the default coverage weight ({@code mu = 1.5}). */
    public CoverageBiasedFrontierUtilitySearch() {
        this(1.5);
    }

    /**
     * Creates an instance with the given coverage weight.
     *
     * @param mu coverage bias weight; must be non-negative
     * @throws IllegalArgumentException if {@code mu} is negative
     */
    public CoverageBiasedFrontierUtilitySearch(double mu) {
        this.mu = requireNonNegativeMu(mu);
    }

    /**
     * Creates an instance with the given coverage weight and benchmark writer.
     *
     * @param mu              coverage bias weight; must be non-negative
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     * @throws IllegalArgumentException if {@code mu} is negative
     */
    protected CoverageBiasedFrontierUtilitySearch(
            double mu,
            BenchmarkWriter<BenchmarkResult> benchmarkWriter) {

        super(benchmarkWriter);
        this.mu = requireNonNegativeMu(mu);
    }

    /**
     * Scores a candidate using orb distance, travel cost, and frontier density.
     *
     * @param candidate       the frontier node being scored
     * @param currentLocation the explorer's current node ID
     * @param travelCost      BFS distances from the current location
     * @param distToOrb       known distances to the Orb for each node
     * @return the coverage-biased utility score; lower is better
     */
    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int density = localFrontierDensity(candidate);

        return orb + travel - mu * density;
    }

    /**
     * Returns the number of known neighbours of {@code candidate} that are
     * currently on the frontier.
     *
     * @param candidate the node to measure density for
     * @return count of frontier-adjacent neighbours
     */
    private int localFrontierDensity(long candidate) {
        int count = 0;

        for (long neighbour : knownNeighbours(candidate)) {
            if (frontier().contains(neighbour)) {
                count++;
            }
        }

        return count;
    }
}