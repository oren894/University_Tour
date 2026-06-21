package tiles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;
import exceptions.InsufficientFundsException;

public class AirportTile implements Tile {

    private static final int TOLL = 100_000;

    private final ArrayList<Tile> tiles;

    public AirportTile(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

    @Override
    public String getName() { return "Airport"; }

    @Override
    public void landOn(Player player) {
        if (!(player instanceof HumanPlayer)) {
            // Bots skip airport
            return;
        }

        if (player.getMoney() < TOLL) {
            JOptionPane.showMessageDialog(null,
                "Airport: You need ₪" + TOLL + " for the toll, but you only have ₪"
                + player.getMoney() + ". Turn ends.",
                "Airport", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build valid destination map: display label → tile index
        Map<String, Integer> destinations = new LinkedHashMap<>();
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t instanceof PropertyTile) {
                PropertyTile pt = (PropertyTile) t;
                if (pt.getOwner() == null || pt.getOwner().equals(player)) {
                    String label = "[" + i + "] " + pt.getName()
                        + (pt.getOwner() == null ? " (unowned)" : " (yours)");
                    destinations.put(label, i);
                }
            } else if (t instanceof ShuttleStationTile) {
                ShuttleStationTile st = (ShuttleStationTile) t;
                if (st.getOwner() == null || st.getOwner().equals(player)) {
                    String label = "[" + i + "] " + st.getName()
                        + (st.getOwner() == null ? " (unowned)" : " (yours)");
                    destinations.put(label, i);
                }
            }
        }

        if (destinations.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "Airport: No valid destinations (all properties are owned by others).\nToll waived.",
                "Airport", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = destinations.keySet().toArray(new String[0]);
        Object result = JOptionPane.showInputDialog(null,
            "Airport! Pay ₪" + TOLL + " to teleport.\nYour money: ₪" + player.getMoney()
            + "\n\nChoose your destination:",
            "Airport — Free Travel", JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (result == null) {
            JOptionPane.showMessageDialog(null, "You skipped the airport. Turn ends.",
                "Airport", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            player.pay(TOLL);
        } catch (InsufficientFundsException e) {
            JOptionPane.showMessageDialog(null, "Not enough money for the toll!", "Airport",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int destIndex = destinations.get(result.toString());
        player.setPosition(destIndex);

        JOptionPane.showMessageDialog(null,
            "Paid ₪" + TOLL + " — teleporting to " + tiles.get(destIndex).getName() + "!",
            "Airport", JOptionPane.INFORMATION_MESSAGE);

        tiles.get(destIndex).landOn(player);
    }
}
