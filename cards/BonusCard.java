package cards;

import players.Player;

// Event card that pays the player a fixed amount.
public class BonusCard extends EventCard {
    private int amount;

    public BonusCard(String description, int amount) {
        this.description = description;
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.receive(amount);
    }
}
