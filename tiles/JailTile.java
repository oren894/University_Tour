package tiles;

import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;

public class JailTile implements Tile {

    @Override
    public String getName() { return "Jail / Just Visiting"; }

    @Override
    public void landOn(Player player) {
        player.sendToJail();
        if (player instanceof HumanPlayer) {
            JOptionPane.showMessageDialog(null,
                player.getName() + " landed on Jail — stuck for 3 turns!\n"
                + "Each turn: pay ₪50,000 fine to escape, or roll doubles.",
                "Jail!", JOptionPane.WARNING_MESSAGE);
        }
    }
}
