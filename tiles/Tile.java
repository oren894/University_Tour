package tiles;

import players.Player;

// A single board space; every tile knows its name and what happens when a player lands on it.
public interface Tile {
    String getName();
    void landOn(Player player);
}
