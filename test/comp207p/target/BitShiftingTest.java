package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.*;

public class BitShiftingTest {
    @Test
    public void shift() throws Exception {
        assertEquals(56, new BitShifting().shift());
    }

}