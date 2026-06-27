package student.searchalg.frontier;

import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

/**
 * Frontier search that selects nodes by minimising orb distance and travel cost.
 *
 * <p>Scores each frontier candidate as {@code orbDistance + travelCost} and
 * navigates to the lowest-scoring node. No replanning occurs mid-navigation.
 *
 * @see BaseFrontierSearch#baseScore(long, Map, Map)
 */
public class FrontierUtilitySearch extends BaseFrontierSearch {

    /** Creates an instance with the default CSV benchmark writer. */
    public FrontierUtilitySearch() {
        super();
    }

    /**
     * Creates an instance with the given benchmark writer.
     *
     * @param benchmarkWriter writer for benchmark results, or {@code null}
     *                        for the default CSV writer
     */
    FrontierUtilitySearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
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
}