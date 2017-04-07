package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.*;

public class AConditionalTest {
    @Test
    public void foo() throws Exception {
        assertEquals(8, new AConditional().foo());
    }

}