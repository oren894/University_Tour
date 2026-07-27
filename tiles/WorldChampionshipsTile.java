package tiles;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;

public class WorldChampionshipsTile implements Tile {

    private static int globalMultiplier = 1;
    private static PropertyTile wcProperty = null;

    @Override
    public String getName() { return "World Championships"; }

    @Override
    public void landOn(Player player) {
        ArrayList<PropertyTile> owned = player.getOwnedProperties();

        if (owned.isEmpty()) {
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    "World Championships!\nYou have no properties to place the effect on.",
                    "World Championships", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        globalMultiplier++;

        if (player instanceof HumanPlayer) {
            // Build display list: "PropertyName (current rent: ₪X)"
            String[] options = new String[owned.size()];
            for (int i = 0; i < owned.size(); i++) {
                PropertyTile p = owned.get(i);
                options[i] = p.getName() + "  —  rent ₪" + String.format("%,d", p.getRent())
                    + "  →  will become ₪" + String.format("%,d", p.getRent() * globalMultiplier);
            }

            Object result = JOptionPane.showInputDialog(null,
                "World Championships! (multiplier now x" + globalMultiplier + ")\n"
                + "Choose a property to place the WC effect on:\n"
                + "(Visitors will pay " + globalMultiplier + "x rent)",
                "World Championships", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

            if (result == null) {
                // Player cancelled — still use first property (mandatory)
                result = options[0];
            }

            int chosenIndex = 0;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(result.toString())) {
                    chosenIndex = i;
                    break;
                }
            }

            applyWCEffect(owned.get(chosenIndex));

            JOptionPane.showMessageDialog(null,
                owned.get(chosenIndex).getName() + " now charges x" + globalMultiplier + " rent!",
                "World Championships", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Bot: apply to highest-rent owned property
            PropertyTile best = owned.get(0);
            for (PropertyTile p : owned) {
                if (p.getRent() > best.getRent()) best = p;
            }
            applyWCEffect(best);
        }
    }

    private void applyWCEffect(PropertyTile chosen) {
        if (wcProperty != null) {
            wcProperty.clearWCEffect();
        }
        wcProperty = chosen;
        wcProperty.setWCEffect(globalMultiplier);
    }

    public static PropertyTile getWcProperty()    { return wcProperty; }
    public static int          getGlobalMultiplier() { return globalMultiplier; }

    public static void reset() {
        globalMultiplier = 1;
        wcProperty = null;
    }
}
