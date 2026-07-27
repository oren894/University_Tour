package tiles;

import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;

// Landing here (not passing through) sends the player to jail for up to 3 turns.
public class JailTile implements Tile {

    @Override
    public String getName() { return "Jail / Just Visiting"; }

    @Override
    public void landOn(Player player) {
        player.sendToJail();
        if (player instanceof HumanPlayer) {
            JOptionPane.showMessageDialog(null,
                player.getName() + " landed on Jail — stuck for 3 turns!\n"
                + "Each turn: pay ₪200,000 fine to escape, or roll doubles.",
                "Jail!", JOptionPane.WARNING_MESSAGE);
        }
    }
}
