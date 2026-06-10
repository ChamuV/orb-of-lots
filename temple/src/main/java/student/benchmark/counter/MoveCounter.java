package student.benchmark.counter;

/**
 * Counts movement steps made during exploration.
 */
public class MoveCounter implements Counter {

    private int count;

    @Override
    public void increment() {
        count++;
    }

    @Override
    public int getValue() {
        return count;
    }
}