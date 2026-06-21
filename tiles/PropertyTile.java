package tiles;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import players.Player;
import players.BotPlayer;
import players.HumanPlayer;
import exceptions.InsufficientFundsException;

public class PropertyTile implements Tile {

    private String name;
    private int price;

    // level: 0=land, 1=1 house, 2=2 houses, 3=3 houses, 4=hotel
    private int level = 0;
    private int houseCost;
    private int hotelCost;
    private int[] rentByLevel; // rent[0..4]

    private Player owner;
    private int wcMultiplier = 1;
    private Color groupColor;

    public PropertyTile(String name, int price, int baseRent) {
        this.name      = name;
        this.price     = price;
        this.houseCost = Math.max(10, price / 2);
        this.hotelCost = price;
        this.rentByLevel = new int[]{
            baseRent,
            baseRent * 3,
            baseRent * 6,
            baseRent * 10,
            baseRent * 16
        };
        this.owner = null;
    }

    // ── Tile interface ────────────────────────────────────────────────────────

    @Override
    public String getName() { return name; }

    @Override
    public void landOn(Player player) {
        if (owner == null) {
            offerPurchase(player);
        } else if (!owner.equals(player)) {
            chargeRent(player);
        } else if (level < 4) {
            offerUpgrade(player);
        }
        // level == 4 (hotel) and owner == player: nothing
    }

    // ── JOptionPane purchase flow ─────────────────────────────────────────────

    private void offerPurchase(Player player) {
        if (player instanceof BotPlayer) {
            if (((BotPlayer) player).wantsToBuy(this)) {
                buyAt(player, 0);
            }
            return;
        }

        boolean canBuy3 = player.hasCompletedFirstLap();
        String[] options = buildPurchaseOptions(player, canBuy3);
        if (options.length <= 1) return; // only "Skip" — can't afford anything

        Object result = JOptionPane.showInputDialog(null,
            name + " is for sale!\nYour money: ₪" + player.getMoney() + "\n\nChoose a level:",
            "Buy Property?", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result == null) return;
        String choice = result.toString();
        if (choice.startsWith("Skip")) return;

        int target = 0;
        if      (choice.startsWith("1 House"))  target = 1;
        else if (choice.startsWith("2 Houses")) target = 2;
        else if (choice.startsWith("3 Houses")) target = 3;

        buyAt(player, target);
    }

    private String[] buildPurchaseOptions(Player player, boolean canBuy3) {
        ArrayList<String> opts = new ArrayList<>();
        int[] costs = {getPurchaseCost(0), getPurchaseCost(1), getPurchaseCost(2), getPurchaseCost(3)};
        String[] labels = {"Land", "1 House", "2 Houses", "3 Houses"};

        for (int i = 0; i < 4; i++) {
            if (i == 3 && !canBuy3) continue; // locked first lap
            if (player.getMoney() >= costs[i]) {
                opts.add(labels[i] + " — ₪" + costs[i] + "  (rent ₪" + rentByLevel[i] + ")");
            }
        }
        opts.add("Skip");
        return opts.toArray(new String[0]);
    }

    private void buyAt(Player player, int targetLevel) {
        int cost = getPurchaseCost(targetLevel);
        try {
            player.pay(cost);
            owner = player;
            level = targetLevel;
            player.addProperty(this);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " bought " + name + " (" + getLevelName() + ") for ₪" + cost + "!");
            }
        } catch (InsufficientFundsException e) {
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null, "Not enough money!");
            }
        }
    }

    // ── JOptionPane upgrade flow ──────────────────────────────────────────────

    private void offerUpgrade(Player player) {
        if (!(player instanceof HumanPlayer)) return; // bots don't upgrade

        if (level == 2 && !player.hasCompletedFirstLap()) {
            JOptionPane.showMessageDialog(null,
                "You own " + name + " (" + getLevelName() + ")\n"
                + "3 Houses is locked — complete your first lap to unlock it.");
            return;
        }

        int cost       = getUpgradeCost();
        String nextLvl = getNextLevelName();

        if (player.getMoney() < cost) {
            JOptionPane.showMessageDialog(null,
                "You own " + name + " (" + getLevelName() + ") — can't afford upgrade (₪" + cost + ").");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "You own " + name + " (" + getLevelName() + ")\n"
            + "Upgrade to " + nextLvl + " for ₪" + cost + "?\n"
            + "New rent: ₪" + rentByLevel[level + 1] + "\n"
            + "Your money: ₪" + player.getMoney(),
            "Upgrade Property?", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                player.pay(cost);
                level++;
                JOptionPane.showMessageDialog(null,
                    name + " upgraded to " + getLevelName() + "!");
            } catch (InsufficientFundsException e) {
                JOptionPane.showMessageDialog(null, "Not enough money to upgrade!");
            }
        }
    }

    // ── Rent ──────────────────────────────────────────────────────────────────

    private void chargeRent(Player player) {
        int rent = getRent() * wcMultiplier;
        String wcNote = wcMultiplier > 1 ? " [WC x" + wcMultiplier + "]" : "";
        try {
            player.pay(rent);
            owner.receive(rent);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " paid ₪" + rent + " rent to " + owner.getName()
                    + " for " + name + " (" + getLevelName() + ")" + wcNote);
            }
        } catch (InsufficientFundsException e) {
            player.setBankrupt(true);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " went bankrupt paying rent on " + name + "!");
            }
        }
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public int getPrice()          { return price; }
    public int getRent()           { return rentByLevel[level]; }
    public int[] getRentByLevel()  { return rentByLevel; }
    public int getLevel()          { return level; }
    public int getHouseCost()      { return houseCost; }
    public int getHotelCost()      { return hotelCost; }
    public Player getOwner()       { return owner; }

    public int getPurchaseCost(int targetLevel) {
        return price + targetLevel * houseCost;
    }

    public int getUpgradeCost() {
        return (level >= 3) ? hotelCost : houseCost;
    }

    public String getLevelName() {
        switch (level) {
            case 0: return "Land";
            case 1: return "1 House";
            case 2: return "2 Houses";
            case 3: return "3 Houses";
            case 4: return "Hotel";
            default: return "?";
        }
    }

    public String getNextLevelName() {
        switch (level) {
            case 0: return "1 House";
            case 1: return "2 Houses";
            case 2: return "3 Houses";
            case 3: return "Hotel";
            default: return "N/A";
        }
    }

    public void setOwner(Player owner)  { this.owner = owner; }
    public void setLevel(int level)     { this.level = level; }
    public void upgrade()               { if (level < 4) level++; }

    public void setWCEffect(int multiplier) { this.wcMultiplier = multiplier; }
    public void clearWCEffect()             { this.wcMultiplier = 1; }
    public int  getWCMultiplier()           { return wcMultiplier; }

    public void setGroupColor(Color c)      { this.groupColor = c; }
    public Color getGroupColor()            { return groupColor; }
}
