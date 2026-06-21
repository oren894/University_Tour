package players;

import java.util.ArrayList;
import game.Board;
import game.Dice;
import tiles.PropertyTile;

public class BotPlayer extends Player {

    public BotPlayer(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.ownedProperties = new ArrayList<>();
        this.ownedStations = new ArrayList<>();
    }

    @Override
    public void takeTurn(Board board, Dice dice) {
        int roll = dice.roll();
        move(roll);
        board.getTile(position).landOn(this);
        // Bot acts silently — no JOptionPane prompts
    }

    public boolean wantsToBuy(PropertyTile property) {
        return property.getPrice() < money * 0.4;
    }
}
