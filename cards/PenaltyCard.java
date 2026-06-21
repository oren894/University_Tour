package cards;

import players.Player;
import exceptions.InsufficientFundsException;

public class PenaltyCard extends EventCard {
    private int amount;

    public PenaltyCard(String description, int amount) {
        this.description = description;
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        try {
            player.pay(amount);
        } catch (InsufficientFundsException e) {
            player.setBankrupt(true);
        }
    }
}
