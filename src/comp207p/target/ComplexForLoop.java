package comp207p.target;

public class ComplexForLoop {
    public long foo(){
        int a = 534245;
        int b = a - 1234;

        long loop1 = 0;
        for(int i = 0; i < 20; i = i + 4){
            loop1 += (b-a) * i;
            System.out.println((b-a) * i);
        }

        long loop2 = 0;
        for(int i = 1; i < 24; i = i * 2){
            loop2 += (b-a) * i;
            System.out.println((b-a) * i);
        }

        long loop3 = 0;
        for(int j = 100; j > 20; j = j - 2){
            loop3 += (b-a) * j;
            System.out.println((b-a) * j);
        }

        return loop1 + loop2 + loop3;
    }
}
