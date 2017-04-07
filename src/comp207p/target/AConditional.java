package comp207p.target;

public class AConditional {
    public int foo(){
        int a = 3;
        if(a == 2){
            a = 4;
        }
        else{
            a = 5;
        }
        int b = 3 + a;
        int c = b + 3;
        a = 4;

        b = a + 4;

        return b;
    }
}
