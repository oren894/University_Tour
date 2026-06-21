package tiles;

import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;
import players.BotPlayer;
import exceptions.InsufficientFundsException;

public class ShuttleStationTile implements Tile {

    private static final int PRICE = 200_000;
    private static final int[] RENT = {25_000, 50_000, 100_000, 200_000};

    private String name;
    private Player owner;

    public ShuttleStationTile(String name) {
        this.name = name;
    }

    @Override
    public String getName() { return name; }

    @Override
    public void landOn(Player player) {
        if (owner == null) {
            offerPurchase(player);
        } else if (!owner.equals(player)) {
            chargeRent(player);
        }
        // owner == player: nothing to do
    }

    private void offerPurchase(Player player) {
        if (player instanceof BotPlayer) {
            if (player.getMoney() >= PRICE) {
                buy(player);
            }
            return;
        }
        if (player.getMoney() < PRICE) {
            JOptionPane.showMessageDialog(null,
                name + " is for sale (₪" + PRICE + ") but you can't afford it.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(null,
            name + " is for sale!\nPrice: ₪" + PRICE
            + "\nYour money: ₪" + player.getMoney()
            + "\n\nBuy it?",
            "Buy Shuttle Station?", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            buy(player);
        }
    }

    private void buy(Player player) {
        try {
            player.pay(PRICE);
            owner = player;
            player.addStation(this);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " bought " + name + " for ₪" + PRICE + "!");
            }
        } catch (InsufficientFundsException e) {
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null, "Not enough money!");
            }
        }
    }

    private void chargeRent(Player player) {
        int stationsOwned = owner.getOwnedStations().size();
        int rent = RENT[Math.min(stationsOwned - 1, 3)];
        try {
            player.pay(rent);
            owner.receive(rent);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " paid ₪" + rent + " rent to " + owner.getName()
                    + " for " + name
                    + " (" + stationsOwned + " station(s) owned)");
            }
        } catch (InsufficientFundsException e) {
            player.setBankrupt(true);
            if (player instanceof HumanPlayer) {
                JOptionPane.showMessageDialog(null,
                    player.getName() + " went bankrupt paying rent on " + name + "!");
            }
        }
    }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    public static int getPrice() { return PRICE; }
    public static int[] getRentSchedule() { return RENT; }

    public int getCurrentRent() {
        if (owner == null) return 0;
        int n = owner.getOwnedStations().size();
        return RENT[Math.min(n - 1, 3)];
    }

    public void guiBuy(Player player) throws exceptions.InsufficientFundsException {
        player.pay(PRICE);
        owner = player;
        player.addStation(this);
    }
}
