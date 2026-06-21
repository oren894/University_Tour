package tiles;

import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;

public class StartTile implements Tile {

    @Override
    public String getName() { return "Start"; }

    @Override
    public void landOn(Player player) {
        player.receive(200_000);
        if (player instanceof HumanPlayer) {
            JOptionPane.showMessageDialog(null, "You landed on Start! Collect ₪200,000!");
        }
    }
}
