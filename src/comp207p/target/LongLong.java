package comp207p.target;

/**
 * Special edge case for adding two long values to return an odd number
 */
public class LongLong {
    public boolean foo() {
        long aa = 400000;
        long a = 10000000000000001L + aa;
        return a == 10000000000400001L;
    }
}
