package student.searchalg.frontier;

import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;

import java.util.Map;

public class FrontierUtilitySearch extends BaseFrontierSearch {

    public FrontierUtilitySearch() {
        super();
    }

    FrontierUtilitySearch(BenchmarkWriter<BenchmarkResult> benchmarkWriter) {
        super(benchmarkWriter);
    }

    @Override
    protected double score(
            long candidate,
            long currentLocation,
            Map<Long, Integer> travelCost,
            Map<Long, Integer> distToOrb) {

        int orb = distToOrb.getOrDefault(candidate, Integer.MAX_VALUE / 2);
        int travel = travelCost.getOrDefault(candidate, Integer.MAX_VALUE / 2);

        return orb + travel;
    }
}