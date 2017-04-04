package comp207p.target;

public class SuperComplexForLoop {
    public int foo() {
        int a = 1;
        for(int i = 0; i < 5; i++){
            if(a == 1){
                a = 2;
            }
            else if(a == 2){
                a = 3;
            }
            else{
                a = 4;
            }
        }

        return a + 3;
    }
}
