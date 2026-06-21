package tiles;

import players.Player;

public interface Tile {
    String getName();
    void landOn(Player player);
}
