package comp207p.target;

public class BreakForLoop {
    public int foo() {
        int a = 3;

        for(int j = 1; j < 10; j++) {
            a = a + j;

            if(j == 5) break;
        }

        return a + 3;
    }
}
