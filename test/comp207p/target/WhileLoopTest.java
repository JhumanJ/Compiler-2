package comp207p.target;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class WhileLoopTest {
    private static final double DELTA = 1e-15;

    WhileLoop whileLoop = new WhileLoop();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams()
    {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams()
    {
        System.setOut(null);
    }

    @Test
    public void testFoo(){
        assertEquals(35.3856, whileLoop.foo(), DELTA);
    }
}
