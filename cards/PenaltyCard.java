package cards;

import players.Player;
import players.HumanPlayer;
import exceptions.InsufficientFundsException;

// Event card that fines the player; if they can't pay, human players get a chance to sell properties first.
public class PenaltyCard extends EventCard {
    private int amount;

    public PenaltyCard(String description, int amount) {
        this.description = description;
        this.amount = amount;
    }

    public int getAmount() { return amount; }

    @Override
    public void apply(Player player) {
        try {
            player.pay(amount);
        } catch (InsufficientFundsException e) {
            // Can't afford the fine: let a human player sell properties and retry once, otherwise go bankrupt.
            if (player instanceof HumanPlayer && ((HumanPlayer) player).trySellPropertiesToPay(amount)) {
                try { player.pay(amount); }
                catch (InsufficientFundsException e2) { player.setBankrupt(true); }
            } else {
                player.setBankrupt(true);
            }
        }
    }
}
