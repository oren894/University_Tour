package cards;

import players.Player;

// Base class for the chance-deck cards a player draws on landing on an event tile.
public abstract class EventCard {
    protected String description;

    public abstract void apply(Player player);

    public String getDescription() {
        return description;
    }
}
