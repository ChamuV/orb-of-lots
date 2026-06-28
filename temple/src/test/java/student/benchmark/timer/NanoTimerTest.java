package student.benchmark.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NanoTimer}.
 *
 * <p>Verifies initial state, that elapsed time is positive after a timed
 * interval, and that stopping before starting does not produce a negative
 * reading. Timing comparisons between two intervals are deliberately avoided
 * because they are sensitive to JVM scheduling and produce flaky results.
 */
class NanoTimerTest {

    @Test
    void elapsedIsZeroBeforeAnyTimingOccurs() {
        NanoTimer timer = new NanoTimer();
        assertEquals(0, timer.getElapsedMicroseconds());
    }

    @Test
    void elapsedIsPositiveAfterTimedInterval() throws InterruptedException {
        NanoTimer timer = new NanoTimer();
        timer.start();
        Thread.sleep(5);
        timer.stop();
        assertTrue(timer.getElapsedMicroseconds() > 0);
    }

    @Test
    void stopBeforeStartReturnsNonNegative() {
        // Both timestamps initialise to 0; elapsed = stop - start = 0.
        NanoTimer timer = new NanoTimer();
        timer.stop();
        assertTrue(timer.getElapsedMicroseconds() >= 0);
    }
}