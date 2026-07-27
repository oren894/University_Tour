package game;

import java.util.Random;

public class Dice {
    private Random random = new Random();
    private int d1, d2;

    public int roll() {
        d1 = random.nextInt(6) + 1;
        d2 = random.nextInt(6) + 1;
        return d1 + d2;
    }

    public boolean isDoubles() { return d1 == d2; }
    public int getD1()         { return d1; }
    public int getD2()         { return d2; }
}
