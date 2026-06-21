package players;

import java.util.ArrayList;
import game.Board;
import game.Dice;
import tiles.PropertyTile;
import tiles.ShuttleStationTile;
import exceptions.InsufficientFundsException;

public abstract class Player implements Comparable<Player>, Cloneable {
    protected String name;
    protected int money;
    protected int position;
    protected boolean bankrupt;
    protected boolean completedFirstLap = false;
    protected ArrayList<PropertyTile> ownedProperties;
    protected ArrayList<ShuttleStationTile> ownedStations;
    protected boolean inJail = false;
    protected int jailTurnsLeft = 0;

    public abstract void takeTurn(Board board, Dice dice);

    public void move(int steps) {
        int oldPosition = position;
        position = (position + steps) % 36;
        if (position < oldPosition) {
            receive(200_000);
            completedFirstLap = true;
        }
    }

    public void pay(int amount) throws InsufficientFundsException {
        if (money < amount) throw new InsufficientFundsException(
            name + " cannot afford ₪" + amount + " (has ₪" + money + ")");
        money -= amount;
    }

    public void receive(int amount) {
        money += amount;
    }

    public void addProperty(PropertyTile p) {
        ownedProperties.add(p);
    }

    public void removeProperty(PropertyTile p) {
        ownedProperties.remove(p);
    }

    public void addStation(ShuttleStationTile s) {
        ownedStations.add(s);
    }

    public ArrayList<ShuttleStationTile> getOwnedStations() { return ownedStations; }

    @Override
    public int compareTo(Player other) {
        return this.money - other.money;
    }

    @Override
    public Player clone() throws CloneNotSupportedException {
        Player copy = (Player) super.clone();
        copy.ownedProperties = new ArrayList<>(this.ownedProperties);
        copy.ownedStations = new ArrayList<>(this.ownedStations);
        return copy;
    }

    @Override
    public String toString() {
        return name + " | ₪" + money + " | pos:" + position + (bankrupt ? " [BANKRUPT]" : "");
    }

    public String getName() { return name; }
    public int getMoney() { return money; }
    public int getPosition() { return position; }
    public boolean isBankrupt() { return bankrupt; }
    public boolean hasCompletedFirstLap() { return completedFirstLap; }
    public ArrayList<PropertyTile> getOwnedProperties() { return ownedProperties; }

    public void sendToJail()        { inJail = true;  jailTurnsLeft = 3; }
    public void releaseFromJail()   { inJail = false; jailTurnsLeft = 0; }
    public void decrementJailTurns(){ if (jailTurnsLeft > 0) jailTurnsLeft--; }
    public boolean isInJail()       { return inJail; }
    public int getJailTurnsLeft()   { return jailTurnsLeft; }

    public void setName(String name) { this.name = name; }
    public void setMoney(int money) { this.money = money; }
    public void setPosition(int position) { this.position = position; }
    public void setBankrupt(boolean bankrupt) { this.bankrupt = bankrupt; }
}
