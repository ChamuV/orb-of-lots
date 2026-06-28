package student.benchmark;

import org.junit.jupiter.api.Test;
import student.benchmark.counter.Counter;
import student.benchmark.timer.Timer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BenchmarkSession}.
 *
 * <p>Uses stub {@link Timer} and {@link Counter} implementations to verify
 * that the session correctly coordinates timing, move counting, seed
 * recording, and success/failure marking without filesystem dependencies.
 */
class BenchmarkSessionTest {

    /** Stub timer that returns a fixed elapsed value. */
    private static class FixedTimer implements Timer {
        private final long elapsedUs;

        FixedTimer(long elapsedUs) {
            this.elapsedUs = elapsedUs;
        }

        @Override public void start() {}
        @Override public void stop() {}
        @Override public long getElapsedMicroseconds() { return elapsedUs; }
    }

    /** Stub counter that counts increments. */
    private static class TrackingCounter implements Counter {
        private int count;

        @Override public void increment() { count++; }
        @Override public int getValue() { return count; }
    }

    @Test
    void recordsMoveCount() {
        TrackingCounter counter = new TrackingCounter();
        BenchmarkSession session = new BenchmarkSession("test", new FixedTimer(0), counter);

        session.recordMove();
        session.recordMove();
        session.recordMove();
        session.stop();

        assertEquals(3, session.getResult().getMoves());
    }

    @Test
    void recordsRuntimeFromTimer() {
        BenchmarkSession session = new BenchmarkSession("test", new FixedTimer(1234), new TrackingCounter());

        session.start();
        session.stop();

        assertEquals(1234, session.getResult().getRuntimeUs());
    }

    @Test
    void markSuccessAndFailureAreReflectedInResult() {
        BenchmarkSession successSession = new BenchmarkSession("test", new FixedTimer(0), new TrackingCounter());
        successSession.markSuccess();
        assertTrue(successSession.getResult().isSuccess());

        BenchmarkSession failSession = new BenchmarkSession("test", new FixedTimer(0), new TrackingCounter());
        failSession.markFailure();
        assertFalse(failSession.getResult().isSuccess());
    }

    @Test
    void seedIsRecordedCorrectly() {
        BenchmarkSession session = new BenchmarkSession("test", new FixedTimer(0), new TrackingCounter());
        session.setSeed(99999L);
        assertEquals(99999L, session.getResult().getSeed());
    }
}