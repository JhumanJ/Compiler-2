package comp207p.target;


public class FunctionDependency {
    private int returnTwelve() {
        return 12;
    }

    public int foo() {
        int a = 242;
        return returnTwelve() * a;
    }
}
