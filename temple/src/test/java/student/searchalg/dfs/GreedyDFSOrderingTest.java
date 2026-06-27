package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.benchmark.writer.BenchmarkWriter;
import student.searchalg.GraphExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying that {@link GreedyDFS} neighbour ordering by distance-to-target
 * actually influences exploration order on branching graphs.
 */
class GreedyDFSOrderingTest {

    private static final BenchmarkWriter<BenchmarkResult> NO_OP = r -> {};

    /**
     * Two branches from node 0; node 1 is the orb with dist=0, node 2 is a
     * dead end with dist=5. GreedyDFS should visit node 1 first and find the
     * orb in exactly 1 move, never touching the dead-end branch.
     */
    @Test
    void selectsCloserNeighbourFirst() {
        BenchmarkResult[] captured = new BenchmarkResult[1];

        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(1)
                .edge(0, 1).edge(0, 2).edge(2, 3)
                .distance(0, 1).distance(1, 0).distance(2, 5).distance(3, 6)
                .build();

        GreedyDFS alg = new GreedyDFS(r -> captured[0] = r);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
        assertEquals(1, captured[0].getMoves(),
                "Greedy ordering should reach the orb in 1 move, skipping the dead-end branch");
    }

    /**
     * Equal distances should not cause infinite loops or crashes.
     */
    @Test
    void handlesEqualDistancesWithoutInfiniteLoop() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(2)
                .edge(0, 1).edge(0, 2)
                .distance(0, 2).distance(1, 2).distance(2, 0)
                .build();

        GreedyDFS alg = new GreedyDFS(NO_OP);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }

    /**
     * Orb is on the further branch; full backtracking still finds it.
     */
    @Test
    void eventuallyFindsOrbEvenOnFurtherBranch() {
        GraphExplorationState state = new GraphExplorationState.Builder()
                .orbAt(3)
                .edge(0, 1).edge(0, 2).edge(2, 3)
                .distance(0, 2).distance(1, 5).distance(2, 2).distance(3, 0)
                .build();

        GreedyDFS alg = new GreedyDFS(NO_OP);
        alg.findOrb(state);

        assertEquals(0, state.getDistanceToTarget());
    }
}