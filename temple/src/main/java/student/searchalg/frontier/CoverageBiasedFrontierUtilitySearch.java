package student.searchalg.frontier;

import java.util.Map;

/**
 * Frontier search with a local frontier-density coverage bonus.
 *
 * <p>Extends the standard utility scoring function with an information-gain
 * proxy: frontier nodes adjacent to several other frontier nodes are treated
 * as gateways into unexplored boundary regions and given a scoring bonus.
 *
 * <p>Scoring function:
 *
 * <pre>
 * score = orbDistance + travelCost - mu * frontierDensity
 * </pre>
 */
public class CoverageBiasedFrontierUtilitySearch extends BaseFrontierSearch {

    private final double mu;

    public CoverageBiasedFrontierUtilitySearch() {
        this(1.5);
    }

    public CoverageBiasedFrontierUtilitySearch(double mu) {
        if (mu < 0) {
            throw new IllegalArgumentException("mu must be non-negative; got " + mu);
        }

        this.mu = mu;
    }

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