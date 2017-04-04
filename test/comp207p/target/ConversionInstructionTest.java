package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConversionInstructionTest {
    @Test
    public void foo() throws Exception {
        assertEquals(108, new ConversionInstruction().foo());
    }

}