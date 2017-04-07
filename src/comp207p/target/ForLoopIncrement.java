package comp207p.target;

/**
 * Check if we can handle IINC instructions in a for loop
 */
public class ForLoopIncrement {
    public long foo() {
        int x = 420;
        for(int i = 0; i < 10; i++) {
            x += 25;
        }

        return (long) x;
    }
}
