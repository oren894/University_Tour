package cards;

import players.Player;

public abstract class EventCard {
    protected String description;

    public abstract void apply(Player player);

    public String getDescription() {
        return description;
    }
}
