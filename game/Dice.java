package game;

import java.util.Random;

public class Dice {
    private Random random = new Random();

    public int roll() {
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        return d1 + d2;
    }
}
