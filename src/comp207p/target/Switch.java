package comp207p.target;

public class Switch {
    public long foo() {
        int var = 2004;

        int bar = var * var * 4 / (var / 2);

        switch (bar) {
            case 16032:
            case 1:
                return bar + 4;
            default:
                return -1;
        }

    }
}
