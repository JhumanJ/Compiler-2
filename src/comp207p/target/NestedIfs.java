package comp207p.target;

public class NestedIfs {
    public int foo() {
        int a = 10;
        int b = 4;


        if(a + b > 4) {
            b+= 14;
            a+= 20;

            if(b + a == 48) {
                return a;
            }
        }

        return -1;
    }
}
