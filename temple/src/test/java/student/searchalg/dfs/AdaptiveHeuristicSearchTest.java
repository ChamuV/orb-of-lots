package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AdaptiveHeuristicSearch}.
 *
 * <p>Verifies that the adaptive heuristic strategy finds the Orb,
 * records moves correctly, and reports success on a simple graph.
 * Heuristic update behaviour is verified implicitly through successful
 * completion.
 */
class AdaptiveHeuristicSearchTest {

    @Test
    void findsOrbOnTwoNodeGraph() {
        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(result -> {});
        StubExplorationState state = new StubExplorationState();

        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    @Test
    void recordsOneMoveOnTwoNodeGraph() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
    }

    @Test
    void reportsSuccessWhenOrbFound() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        AdaptiveHeuristicSearch alg = new AdaptiveHeuristicSearch(r -> captured[0] = r);

        alg.findOrb(new StubExplorationState());

        assertTrue(captured[0].isSuccess());
    }
}