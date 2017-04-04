package comp207p.target;

public class IfBranches {
    public int returnTwelve() {
        return 12;
    }

    public int foo() {
        int a = 21;

        if(returnTwelve() == 24) {//returns false
            a = 12;
        }

        return a;
    }
}
