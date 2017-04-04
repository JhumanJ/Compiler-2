package comp207p.target;

public class BConditional{
    public int foo(){
        int a = 5;

        if(returnFalse()){
            a = 4;
        }

        return a + 3;
    }

    public boolean returnFalse(){
        return false;
    }
}
