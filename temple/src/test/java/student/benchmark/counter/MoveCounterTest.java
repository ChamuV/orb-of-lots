package student.benchmark.counter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MoveCounter}.
 *
 * <p>Verifies initial state and cumulative increment behaviour.
 */
class MoveCounterTest {

    @Test
    void startsAtZero() {
        MoveCounter counter = new MoveCounter();
        assertEquals(0, counter.getValue());
    }

    @Test
    void incrementsCorrectly() {
        MoveCounter counter = new MoveCounter();
        counter.increment();
        counter.increment();
        counter.increment();
        assertEquals(3, counter.getValue());
    }

    @Test
    void eachIncrementAddsOne() {
        MoveCounter counter = new MoveCounter();
        for (int i = 1; i <= 10; i++) {
            counter.increment();
            assertEquals(i, counter.getValue());
        }
    }
}