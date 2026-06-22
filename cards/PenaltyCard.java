package cards;

import players.Player;
import players.HumanPlayer;
import exceptions.InsufficientFundsException;

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
            if (player instanceof HumanPlayer && ((HumanPlayer) player).trySellPropertiesToPay(amount)) {
                try { player.pay(amount); }
                catch (InsufficientFundsException e2) { player.setBankrupt(true); }
            } else {
                player.setBankrupt(true);
            }
        }
    }
}
