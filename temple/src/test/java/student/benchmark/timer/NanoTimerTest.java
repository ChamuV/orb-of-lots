package student.benchmark.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NanoTimer}.
 *
 * <p>Verifies that elapsed time is positive after a timed interval,
 * zero before timing begins, and increases across successive measurements.
 */
class NanoTimerTest {

    @Test
    void elapsedIsPositiveAfterDelay() throws InterruptedException {
        NanoTimer timer = new NanoTimer();
        timer.start();
        Thread.sleep(5);
        timer.stop();
        assertTrue(timer.getElapsedMicroseconds() > 0);
    }

    @Test
    void elapsedIsZeroWithoutStartStop() {
        NanoTimer timer = new NanoTimer();
        assertEquals(0, timer.getElapsedMicroseconds());
    }

    @Test
    void longerDelayProducesLargerElapsed() throws InterruptedException {
        NanoTimer shortTimer = new NanoTimer();
        shortTimer.start();
        Thread.sleep(5);
        shortTimer.stop();

        NanoTimer longTimer = new NanoTimer();
        longTimer.start();
        Thread.sleep(20);
        longTimer.stop();

        assertTrue(longTimer.getElapsedMicroseconds() > shortTimer.getElapsedMicroseconds());
    }

    @Test
    void elapsedIsPositiveAfterStartStopWithSleep() throws InterruptedException {
        NanoTimer timer = new NanoTimer();
        timer.start();
        Thread.sleep(1);
        timer.stop();

        assertTrue(timer.getElapsedMicroseconds() > 0);
    }

    @Test
    void stopBeforeStartReturnsNonNegative() {
        // Both timestamps default to 0; stop - start = 0.
        NanoTimer timer = new NanoTimer();
        timer.stop();

        assertTrue(timer.getElapsedMicroseconds() >= 0);
    }

    @Test
    void restartReflectsNewInterval() throws InterruptedException {
        NanoTimer timer = new NanoTimer();

        timer.start();
        Thread.sleep(1);
        timer.stop();
        long firstInterval = timer.getElapsedMicroseconds();

        // Second interval: start and stop immediately — should be shorter.
        timer.start();
        timer.stop();
        long secondInterval = timer.getElapsedMicroseconds();

        assertTrue(firstInterval >= secondInterval);
    }
}