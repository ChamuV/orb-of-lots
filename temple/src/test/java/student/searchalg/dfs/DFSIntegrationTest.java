package student.searchalg.dfs;

import org.junit.jupiter.api.Test;
import student.benchmark.BenchmarkResult;
import student.searchalg.StubExplorationState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DFS} verifying the full
 * {@link student.searchalg.Algorithm} template-method pipeline.
 *
 * <p>Confirms that move counting, success detection, and benchmark
 * writer invocation all work correctly end to end.
 */
class DFSIntegrationTest {

    @Test
    void writerIsInvokedAfterSearch() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        DFS dfs = new DFS(r -> captured[0] = r);

        dfs.findOrb(new StubExplorationState());

        assertNotNull(captured[0]);
    }

    @Test
    void moveCountAndSuccessAreRecordedTogether() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        DFS dfs = new DFS(r -> captured[0] = r);

        dfs.findOrb(new StubExplorationState());

        assertEquals(1, captured[0].getMoves());
        assertTrue(captured[0].isSuccess());
    }

    @Test
    void algorithmNameIsSetCorrectly() {
        BenchmarkResult[] captured = new BenchmarkResult[1];
        DFS dfs = new DFS(r -> captured[0] = r);

        dfs.findOrb(new StubExplorationState());

        assertEquals("DFS", captured[0].getAlgorithmName());
    }
}