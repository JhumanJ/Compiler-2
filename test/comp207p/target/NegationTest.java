package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.*;

public class NegationTest {
    private static final double DELTA = 1e-15;

    @Test
    public void foo() throws Exception {
        assertEquals(-0.5343, new Negation().foo(), DELTA);
    }

}