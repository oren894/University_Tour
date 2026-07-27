package players;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import game.Board;
import game.Dice;
import tiles.PropertyTile;
import tiles.ShuttleStationTile;

// Player controlled via JOptionPane dialogs; makes its own buy/sell/jail decisions through the UI.
public class HumanPlayer extends Player {

    public HumanPlayer(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.ownedProperties = new ArrayList<>();
        this.ownedStations = new ArrayList<>();
    }

    // Prompts the player to sell owned properties/stations one at a time until the debt is covered.
    public boolean trySellPropertiesToPay(int amount) {
        while (money < amount) {
            if (ownedProperties.isEmpty() && ownedStations.isEmpty()) return false;

            ArrayList<String> options = new ArrayList<>();
            ArrayList<Object> items   = new ArrayList<>();
            for (PropertyTile pt : ownedProperties) {
                options.add(pt.getName() + " (" + pt.getLevelName() + ") → ₪" + String.format("%,d", pt.getSellValue()));
                items.add(pt);
            }
            for (ShuttleStationTile st : ownedStations) {
                options.add(st.getName() + " (Station) → ₪" + String.format("%,d", st.getSellValue()));
                items.add(st);
            }
            options.add("Declare Bankruptcy");

            Object result = JOptionPane.showInputDialog(null,
                name + " — you owe ₪" + String.format("%,d", amount)
                + " but only have ₪" + String.format("%,d", money)
                + "\nShortage: ₪" + String.format("%,d", amount - money)
                + "\n\nChoose a property to sell (75% of purchase value):",
                "Sell Property", JOptionPane.WARNING_MESSAGE,
                null, options.toArray(), options.get(0));

            if (result == null || result.toString().equals("Declare Bankruptcy")) return false;

            int idx = options.indexOf(result.toString());
            Object item = items.get(idx);
            if (item instanceof PropertyTile) {
                PropertyTile pt = (PropertyTile) item;
                JOptionPane.showMessageDialog(null,
                    "Sold " + pt.getName() + " for ₪" + String.format("%,d", pt.getSellValue()) + "!\nBalance: ₪" + String.format("%,d", money + pt.getSellValue()));
                sellProperty(pt);
            } else if (item instanceof ShuttleStationTile) {
                ShuttleStationTile st = (ShuttleStationTile) item;
                JOptionPane.showMessageDialog(null,
                    "Sold " + st.getName() + " for ₪" + String.format("%,d", st.getSellValue()) + "!\nBalance: ₪" + String.format("%,d", money + st.getSellValue()));
                sellStation(st);
            }
        }
        return true;
    }

    @Override
    public void takeTurn(Board board, Dice dice) {
        // ── Announce turn ─────────────────────────────────────────────────────
        String jailTag = inJail ? "\n[IN JAIL — " + jailTurnsLeft + " turn(s) remaining]" : "";
        int proceed = JOptionPane.showConfirmDialog(null,
            name + "'s turn!\nMoney: ₪" + String.format("%,d", money)
            + "  |  Position: " + position + jailTag
            + "\n\nPress OK to roll the dice.",
            "Your Turn", JOptionPane.OK_CANCEL_OPTION);
        if (proceed != JOptionPane.OK_OPTION) System.exit(0);

        // ── Optional jail bail ────────────────────────────────────────────────
        if (inJail) {
            int choice = JOptionPane.showOptionDialog(null,
                name + " is in JAIL! (" + jailTurnsLeft + " turn(s) remaining)\n"
                + "Pay ₪200,000 to escape now, or roll for doubles?",
                "Jail", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{"Pay ₪200,000 & Roll", "Roll for Doubles"}, "Roll for Doubles");

            if (choice == 0) {
                boolean paid = false;
                try {
                    pay(200_000);
                    paid = true;
                } catch (exceptions.InsufficientFundsException e) {
                    if (trySellPropertiesToPay(200_000)) {
                        try { pay(200_000); paid = true; }
                        catch (exceptions.InsufficientFundsException e2) { /* won't happen */ }
                    }
                }
                if (paid) {
                    releaseFromJail();
                    JOptionPane.showMessageDialog(null, name + " paid ₪200,000 — released from jail!");
                } else {
                    JOptionPane.showMessageDialog(null, name + " can't afford the fine — rolling for doubles.");
                }
            }
        }

        // ── Roll dice ─────────────────────────────────────────────────────────
        int roll    = dice.roll();
        int d1      = dice.getD1(), d2 = dice.getD2();
        boolean doubles = dice.isDoubles();

        JOptionPane.showMessageDialog(null,
            name + " rolled " + roll + "  (" + d1 + " + " + d2 + ")"
            + (doubles ? " — DOUBLES!" : ""),
            "Dice Roll", JOptionPane.INFORMATION_MESSAGE);

        // ── Resolve jail post-roll ────────────────────────────────────────────
        if (inJail) {
            if (doubles) {
                releaseFromJail();
                JOptionPane.showMessageDialog(null, name + " rolled doubles — escaped jail!");
            } else {
                decrementJailTurns();
                if (jailTurnsLeft <= 0) {
                    JOptionPane.showMessageDialog(null,
                        name + " served 3 turns — forced out of jail. Must pay ₪200,000.");
                    boolean paid = false;
                    try {
                        pay(200_000);
                        paid = true;
                    } catch (exceptions.InsufficientFundsException e) {
                        if (trySellPropertiesToPay(200_000)) {
                            try { pay(200_000); paid = true; }
                            catch (exceptions.InsufficientFundsException e2) { setBankrupt(true); }
                        } else {
                            setBankrupt(true);
                        }
                    }
                    releaseFromJail();
                    if (paid) JOptionPane.showMessageDialog(null, name + " paid the fine and is free!");
                } else {
                    JOptionPane.showMessageDialog(null,
                        name + " stays in jail. " + jailTurnsLeft + " turn(s) remaining.");
                    return; // no movement this turn
                }
            }
        }

        // ── Move ──────────────────────────────────────────────────────────────
        if (!isBankrupt()) {
            int oldPos = position;
            move(roll);
            if (position < oldPos)
                JOptionPane.showMessageDialog(null, name + " passed Start — collected ₪200,000!");
            JOptionPane.showMessageDialog(null,
                name + " moved to [" + position + "] " + board.getTile(position).getName());
            board.getTile(position).landOn(this);
        }
    }
}
