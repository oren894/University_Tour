package players;

import java.util.ArrayList;
import game.Board;
import game.Dice;
import tiles.PropertyTile;
import exceptions.InsufficientFundsException;

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
        if (inJail) {
            int roll    = dice.roll();
            boolean doubles = dice.isDoubles();

            if (doubles) {
                releaseFromJail();
                // fall through to move with this roll
            } else {
                decrementJailTurns();
                if (jailTurnsLeft <= 0) {
                    // Forced out — bots don't sell, just pay or go bankrupt
                    try { pay(200_000); }
                    catch (InsufficientFundsException e) { setBankrupt(true); }
                    releaseFromJail();
                    // fall through to move with this roll
                } else {
                    return; // stays in jail, skip turn
                }
            }

            if (!isBankrupt()) {
                move(roll);
                board.getTile(position).landOn(this);
            }
            return;
        }

        int roll = dice.roll();
        move(roll);
        board.getTile(position).landOn(this);
    }

    public boolean wantsToBuy(PropertyTile property) {
        return property.getPrice() < money * 0.4;
    }
}
