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

        return baseScore(candidate, travelCost, distToOrb);
    }
}