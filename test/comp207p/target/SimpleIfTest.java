package comp207p.target;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class SimpleIfTest {
    SimpleIf simpleIf = new SimpleIf();

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
        assertEquals(true, simpleIf.foo());
    }
}
