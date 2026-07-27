package gui;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import game.Board;
import game.Dice;
import players.Player;
import players.HumanPlayer;
import players.BotPlayer;
import tiles.*;
import cards.EventCard;
import cards.PenaltyCard;
import exceptions.InsufficientFundsException;

// GUI equivalent of Game: drives turns through button clicks instead of blocking JOptionPane calls,
// so the current state (below) tracks what UI the sidebar should show and what a click means right now.
public class GUIGame {

    public enum State {
        WAITING_ROLL, WAITING_INITIAL_BUY, WAITING_UPGRADE, WAITING_COLLECT,
        WAITING_STATION_BUY, BOT_TURN, GAME_OVER
    }

    private ArrayList<Player> players = new ArrayList<>();
    private Board board;
    private final Dice dice = new Dice();
    private int currentPlayerIndex = 0;
    private int currentRound = 1;
    private final int maxRounds = Integer.MAX_VALUE;
    private State state = State.WAITING_ROLL;

    private PropertyTile                  pendingProperty;
    private EventCard                     pendingEventCard;
    private tiles.ShuttleStationTile      pendingStation;

    private final GameWindow window;

    public GUIGame(GameWindow window) { this.window = window; }

    // ── Setup ─────────────────────────────────────────────────────────────────

    public void start() { setupCards(); setupPlayers(); }

    private void setupCards() {
        ArrayList<EventCard> chance = new ArrayList<>();
        chance.add(new cards.BonusCard("Scholarship awarded — collect ₪100,000", 100_000));
        chance.add(new cards.BonusCard("Won the hackathon — collect ₪150,000", 150_000));
        chance.add(new cards.BonusCard("Research grant approved — collect ₪75,000", 75_000));
        chance.add(new cards.BonusCard("Tuition reimbursement — collect ₪80,000", 80_000));
        chance.add(new cards.BonusCard("Dean's list bonus — collect ₪50,000", 50_000));
        chance.add(new cards.BonusCard("TA salary payment — collect ₪60,000", 60_000));
        chance.add(new cards.PenaltyCard("Lab equipment fine — pay ₪40,000", 40_000));
        chance.add(new cards.PenaltyCard("Library late fees — pay ₪30,000", 30_000));
        chance.add(new cards.PenaltyCard("Parking ticket on campus — pay ₪25,000", 25_000));
        chance.add(new cards.PenaltyCard("Failed experiment — pay ₪50,000", 50_000));
        chance.add(new cards.PenaltyCard("Mandatory campus fund — pay ₪75,000", 75_000));
        chance.add(new cards.PenaltyCard("Disciplinary committee fine — pay ₪60,000", 60_000));

        board = new Board(chance);
    }

    private void setupPlayers() {
        String input = JOptionPane.showInputDialog(window,
            "How many players? (2-4):", "Business Tour", JOptionPane.QUESTION_MESSAGE);
        if (input == null) System.exit(0);

        int count;
        try {
            count = Integer.parseInt(input.trim());
            if (count < 2 || count > 4) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(window, "Enter a number between 2 and 4.");
            setupPlayers(); return;
        }

        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog(window,
                "Name for player " + (i + 1) + ":", "Setup", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Player " + (i + 1);

            Object[] opts = {"Human", "Bot"};
            int choice = JOptionPane.showOptionDialog(window,
                "Is " + name.trim() + " human or bot?", "Player Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);

            players.add(choice == 1 ? new BotPlayer(name.trim(), 2_000_000)
                                    : new HumanPlayer(name.trim(), 2_000_000));
        }

        state = State.WAITING_ROLL;
        window.updateBoard();
        window.updateSidebar();
        window.log("=== Game started with " + players.size() + " players ===");
        beginTurn();
    }

    // ── Turn flow ─────────────────────────────────────────────────────────────

    private void beginTurn() {
        Player p = getCurrentPlayer();
        window.log("— " + p.getName() + "'s turn  (Round " + currentRound + "/" + maxRounds + ")");
        window.updateSidebar();

        if (p instanceof BotPlayer) {
            state = State.BOT_TURN;
            window.setRollEnabled(false);
            Timer t = new Timer(1200, e -> handleRoll());
            t.setRepeats(false);
            t.start();
        } else {
            state = State.WAITING_ROLL;
            window.setRollEnabled(true);
        }
    }

    public void handleRoll() {
        if (state != State.WAITING_ROLL && state != State.BOT_TURN) return;
        window.setRollEnabled(false);

        Player p = getCurrentPlayer();

        // ── Jail handling ────────────────────────────────────────────────────────
        if (p.isInJail()) {
            if (p instanceof HumanPlayer) {
                int choice = JOptionPane.showOptionDialog(window,
                    p.getName() + " is in JAIL! (" + p.getJailTurnsLeft() + " turn(s) remaining)\n"
                    + "Pay ₪200,000 to escape now, or roll for doubles?",
                    "Jail", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new Object[]{"Pay ₪200,000 & Roll", "Roll for Doubles"}, "Roll for Doubles");
                if (choice == 0) {
                    try {
                        p.pay(200_000);
                        p.releaseFromJail();
                        window.log(p.getName() + " paid ₪200,000 — released from jail!");
                        window.updateSidebar();
                    } catch (exceptions.InsufficientFundsException e) {
                        window.log(p.getName() + " can't afford the fine — rolling for doubles.");
                    }
                }
            }
        }

        int d1 = new java.util.Random().nextInt(6) + 1;
        int d2 = new java.util.Random().nextInt(6) + 1;
        int roll = d1 + d2;
        boolean doubles = (d1 == d2);

        window.setDiceResult(d1, d2);
        window.log(p.getName() + " rolled " + roll + "  (" + d1 + "+" + d2 + ")"
            + (doubles ? " — DOUBLES!" : ""));

        // ── Still in jail after roll? ────────────────────────────────────────────
        if (p.isInJail()) {
            if (doubles) {
                p.releaseFromJail();
                window.log(p.getName() + " rolled doubles — escaped jail!");
            } else {
                p.decrementJailTurns();
                int left = p.getJailTurnsLeft();
                if (left <= 0) {
                    if (!tryForcePay(p, 200_000)) {
                        window.log(p.getName() + " couldn't pay the jail fine — bankrupt!");
                    }
                    p.releaseFromJail();
                    if (!p.isBankrupt()) window.log(p.getName() + " served 3 turns — forced out, ₪200,000 fine paid.");
                } else {
                    window.log(p.getName() + " stays in jail. " + left + " turn(s) left.");
                    window.updateSidebar();
                    endTurn();
                    return;
                }
            }
        }

        int oldPos = p.getPosition();
        p.move(roll);
        int newPos = p.getPosition();

        if (newPos < oldPos) window.log(p.getName() + " passed Start — collected ₪200,000!");

        window.updateBoard();
        window.log("Landed on [" + newPos + "] " + board.getTile(newPos).getName());
        window.updateSidebar();

        handleTileLanding(p);
    }

    private void handleTileLanding(Player p) {
        Tile tile = board.getTile(p.getPosition());

        if (tile instanceof PropertyTile) {
            PropertyTile pt = (PropertyTile) tile;

            if (pt.getOwner() == null) {
                if (p instanceof BotPlayer) {
                    if (((BotPlayer) p).wantsToBuy(pt)) doBuyAt(p, pt, 0);
                    else window.log(p.getName() + " passed on " + pt.getName());
                    endTurn();
                } else {
                    pendingProperty = pt;
                    state = State.WAITING_INITIAL_BUY;
                    window.showInitialBuyOffer(pt, p.hasCompletedFirstLap(), p.getMoney());
                }

            } else if (!pt.getOwner().equals(p)) {
                chargeRent(p, pt);
                endTurn();

            } else if (pt.getLevel() < 4) {
                if (p instanceof BotPlayer) {
                    endTurn();
                } else {
                    pendingProperty = pt;
                    state = State.WAITING_UPGRADE;
                    window.showUpgradeOffer(pt, p.hasCompletedFirstLap());
                }
            } else {
                window.log(p.getName() + " has a hotel on " + pt.getName() + ".");
                endTurn();
            }

        } else if (tile instanceof EventTile) {
            EventCard card = ((EventTile) tile).drawCard();
            if (p instanceof BotPlayer) {
                card.apply(p);
                window.log(p.getName() + " drew: " + card.getDescription());
                if (p.isBankrupt()) window.log(p.getName() + " went bankrupt!");
                window.updateSidebar();
                endTurn();
            } else {
                pendingEventCard = card;
                state = State.WAITING_COLLECT;
                window.showCollectCard(tile.getName(), card.getDescription());
            }

        } else if (tile instanceof StartTile) {
            p.receive(200_000);
            window.log(p.getName() + " landed on Start — collected ₪200,000!");
            if (p instanceof HumanPlayer) {
                window.updateSidebar();
                RouletteDialog rd = new RouletteDialog(window, p);
                rd.setVisible(true);
                int net = rd.getNetResult();
                if (net > 0) {
                    p.receive(net);
                    window.log(p.getName() + " won ₪" + String.format("%,d", net) + " at the casino!");
                } else if (net < 0) {
                    if (tryForcePay(p, -net)) {
                        window.log(p.getName() + " lost ₪" + String.format("%,d", -net) + " at the casino.");
                    } else {
                        window.log(p.getName() + " went bankrupt at the casino!");
                    }
                }
            }
            window.updateSidebar();
            endTurn();

        } else if (tile instanceof tiles.ShuttleStationTile) {
            tiles.ShuttleStationTile st = (tiles.ShuttleStationTile) tile;
            if (st.getOwner() == null) {
                if (p instanceof BotPlayer) {
                    if (p.getMoney() >= tiles.ShuttleStationTile.getPrice()) {
                        try {
                            st.guiBuy(p);
                            window.log(p.getName() + " bought " + st.getName() + " for ₪200,000!");
                        } catch (InsufficientFundsException e) { /* skip */ }
                    } else {
                        window.log(p.getName() + " can't afford " + st.getName() + ".");
                    }
                    window.updateSidebar();
                    endTurn();
                } else {
                    pendingStation = st;
                    state = State.WAITING_STATION_BUY;
                    window.showStationBuyOffer(st, p.getMoney());
                }
            } else if (!st.getOwner().equals(p)) {
                int rent = st.getCurrentRent();
                if (tryForcePay(p, rent)) {
                    st.getOwner().receive(rent);
                    window.log(p.getName() + " paid ₪" + String.format("%,d", rent) + " shuttle rent → "
                        + st.getOwner().getName()
                        + "  (" + st.getOwner().getOwnedStations().size() + " station(s))");
                } else {
                    window.log(p.getName() + " went bankrupt on shuttle rent!");
                }
                window.updateSidebar();
                endTurn();
            } else {
                window.log(p.getName() + " owns " + st.getName() + ".");
                endTurn();
            }

        } else if (tile instanceof tiles.AirportTile) {
            tile.landOn(p);
            window.updateBoard();
            window.updateSidebar();
            endTurn();

        } else if (tile instanceof tiles.WorldChampionshipsTile) {
            tile.landOn(p);
            window.updateSidebar();
            endTurn();

        } else if (tile instanceof tiles.JailTile) {
            p.sendToJail();
            window.log(p.getName() + " landed on Jail — stuck for 3 turns!");
            window.updateSidebar();
            endTurn();
        } else {
            endTurn();
        }
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    /** targetLevel 0–3 to buy at that level, -1 to skip. */
    public void handleInitialBuyLevel(int targetLevel) {
        if (state != State.WAITING_INITIAL_BUY || pendingProperty == null) return;
        Player p = getCurrentPlayer();

        if (targetLevel >= 0) {
            if (targetLevel == 3 && !p.hasCompletedFirstLap()) {
                window.log("3 Houses locked — finish lap 1 first!");
                return;
            }
            doBuyAt(p, pendingProperty, targetLevel);
        } else {
            window.log(p.getName() + " passed on " + pendingProperty.getName());
        }

        pendingProperty = null;
        window.hidePurchasePanel();
        endTurn();
    }

    /** doUpgrade=true to upgrade one level, false to keep as-is. */
    public void handleUpgradeDecision(boolean doUpgrade) {
        if (state != State.WAITING_UPGRADE || pendingProperty == null) return;
        Player p = getCurrentPlayer();

        if (doUpgrade) {
            if (pendingProperty.getLevel() == 2 && !p.hasCompletedFirstLap()) {
                window.log("3 Houses locked — finish lap 1 first!");
                pendingProperty = null;
                window.hideUpgradePanel();
                endTurn();
                return;
            }
            try {
                p.pay(pendingProperty.getUpgradeCost());
                pendingProperty.upgrade();
                window.log(p.getName() + " upgraded " + pendingProperty.getName()
                    + " to " + pendingProperty.getLevelName());
            } catch (InsufficientFundsException e) {
                window.log("Not enough money to upgrade!");
            }
        }

        pendingProperty = null;
        window.hideUpgradePanel();
        window.updateSidebar();
        endTurn();
    }

    /** Player clicks Buy or Skip on a shuttle station offer. */
    public void handleStationBuy(boolean doBuy) {
        if (state != State.WAITING_STATION_BUY || pendingStation == null) return;
        Player p = getCurrentPlayer();

        if (doBuy) {
            try {
                pendingStation.guiBuy(p);
                window.log(p.getName() + " bought " + pendingStation.getName() + " for ₪200,000!");
            } catch (InsufficientFundsException e) {
                window.log("Not enough money to buy " + pendingStation.getName() + "!");
            }
        } else {
            window.log(p.getName() + " skipped " + pendingStation.getName() + ".");
        }

        pendingStation = null;
        window.hideStationPanel();
        window.updateSidebar();
        endTurn();
    }

    /** Player clicks Collect on an event card. */
    public void handleCollect() {
        if (state != State.WAITING_COLLECT || pendingEventCard == null) return;
        Player p = getCurrentPlayer();

        if (pendingEventCard instanceof PenaltyCard) {
            int cost = ((PenaltyCard) pendingEventCard).getAmount();
            if (tryForcePay(p, cost)) {
                window.log("Paid: " + pendingEventCard.getDescription());
            } else {
                window.log(p.getName() + " went bankrupt on event card!");
            }
        } else {
            pendingEventCard.apply(p);
            window.log("Collected: " + pendingEventCard.getDescription());
        }
        if (p.isBankrupt()) window.log(p.getName() + " went bankrupt!");

        pendingEventCard = null;
        window.hideCollectPanel();
        window.updateSidebar();
        endTurn();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void doBuyAt(Player p, PropertyTile pt, int targetLevel) {
        int cost = pt.getPurchaseCost(targetLevel);
        try {
            p.pay(cost);
            pt.setOwner(p);
            pt.setLevel(targetLevel);
            p.addProperty(pt);
            window.log(p.getName() + " bought " + pt.getName()
                + " (" + pt.getLevelName() + ") for ₪" + String.format("%,d", cost));
        } catch (InsufficientFundsException e) {
            window.log(p.getName() + " can't afford " + pt.getName());
        }
        window.updateSidebar();
    }

    private void chargeRent(Player p, PropertyTile pt) {
        int rent = pt.getRent() * pt.getWCMultiplier();
        String wcNote = pt.getWCMultiplier() > 1 ? " [WC x" + pt.getWCMultiplier() + "]" : "";
        if (tryForcePay(p, rent)) {
            pt.getOwner().receive(rent);
            window.log(p.getName() + " paid ₪" + String.format("%,d", rent) + " rent to "
                + pt.getOwner().getName() + "  [" + pt.getName() + " — " + pt.getLevelName() + "]" + wcNote);
        } else {
            window.log(p.getName() + " went bankrupt paying rent on " + pt.getName() + "!");
        }
        window.updateSidebar();
    }

    private boolean tryForcePay(Player p, int amount) {
        try {
            p.pay(amount);
            return true;
        } catch (InsufficientFundsException e) {
            if (p.getOwnedProperties().isEmpty() && p.getOwnedStations().isEmpty()) {
                p.setBankrupt(true);
                return false;
            }
            window.log(p.getName() + " is ₪" + String.format("%,d", amount - p.getMoney())
                + " short — may sell properties.");
            SellPropertiesDialog dlg = new SellPropertiesDialog(window, p, amount);
            dlg.setVisible(true);
            try {
                p.pay(amount);
                return true;
            } catch (InsufficientFundsException e2) {
                p.setBankrupt(true);
                return false;
            }
        }
    }

    private void endTurn() {
        window.updateBoard();
        window.updateSidebar();

        long active = players.stream().filter(pl -> !pl.isBankrupt()).count();
        if (active <= 1) { endGame(); return; }

        int next = (currentPlayerIndex + 1) % players.size();
        while (players.get(next).isBankrupt()) next = (next + 1) % players.size();
        if (next <= currentPlayerIndex) currentRound++;
        currentPlayerIndex = next;

        beginTurn();
    }

    private void endGame() {
        state = State.GAME_OVER;
        Collections.sort(players);
        window.log("");
        window.log("=== GAME OVER after round " + currentRound + " ===");
        for (int i = players.size() - 1; i >= 0; i--) {
            Player p = players.get(i);
            window.log((players.size() - i) + ". " + p.getName()
                + " — ₪" + String.format("%,d", p.getMoney()) + (p.isBankrupt() ? " [BANKRUPT]" : ""));
        }
        window.showGameOver();
        window.updateSidebar();
    }

    // ── Building levels (for BoardPanel) ──────────────────────────────────────

    public int[] getBuildingLevels() {
        int[] levels = new int[36];
        java.util.Arrays.fill(levels, -1);
        if (board == null) return levels;
        for (int i = 0; i < 36; i++) {
            Tile t = board.getTile(i);
            if (t instanceof PropertyTile) {
                PropertyTile pt = (PropertyTile) t;
                if (pt.getOwner() != null) levels[i] = pt.getLevel();
            }
        }
        return levels;
    }

    /** Current effective rent for each property tile (base if unowned, levelled if owned, WC-multiplied if active). */
    public int[] getTileRent() {
        int[] rent = new int[36];
        java.util.Arrays.fill(rent, -1);
        if (board == null) return rent;
        for (int i = 0; i < 36; i++) {
            Tile t = board.getTile(i);
            if (t instanceof PropertyTile) {
                PropertyTile pt = (PropertyTile) t;
                rent[i] = pt.getRent() * pt.getWCMultiplier();
            }
        }
        return rent;
    }

    /** Board index of the tile with an active WC multiplier, or -1 if none. */
    public int getWCTileIndex() {
        tiles.PropertyTile wcp = tiles.WorldChampionshipsTile.getWcProperty();
        if (wcp == null || board == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (board.getTile(i) == wcp) return i;
        }
        return -1;
    }

    /** Returns player index (0-based) of whoever owns each tile, or -1 if unowned/N/A. */
    public int[] getTileOwnerIndex() {
        int[] owners = new int[36];
        java.util.Arrays.fill(owners, -1);
        if (board == null) return owners;
        for (int i = 0; i < 36; i++) {
            Tile t = board.getTile(i);
            Player owner = null;
            if (t instanceof PropertyTile) owner = ((PropertyTile) t).getOwner();
            else if (t instanceof tiles.ShuttleStationTile) owner = ((tiles.ShuttleStationTile) t).getOwner();
            if (owner == null) continue;
            for (int pi = 0; pi < players.size(); pi++) {
                if (players.get(pi).equals(owner)) { owners[i] = pi; break; }
            }
        }
        return owners;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Player getCurrentPlayer()      { return players.get(currentPlayerIndex); }
    public ArrayList<Player> getPlayers() { return players; }
    public int getCurrentRound()          { return currentRound; }
    public int getMaxRounds()             { return maxRounds; }
    public State getState()               { return state; }
}
