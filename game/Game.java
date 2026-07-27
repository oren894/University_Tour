package game;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;
import players.BotPlayer;
import cards.EventCard;
import cards.BonusCard;
import cards.PenaltyCard;
import exceptions.InvalidPlayerCountException;

// Console/JOptionPane game loop: sets up players and board, runs turns until one player remains, then shows results.
public class Game {
    private ArrayList<Player> players;
    private Board board;
    private Dice dice;
    private boolean gameOver;
    private int currentRound = 0;

    public Game() {
        players = new ArrayList<>();
        dice = new Dice();
    }

    public void start() {
        try {
            setupCards();
            setupPlayers();
            playLoop();
            showResults();
        } catch (InvalidPlayerCountException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void setupCards() {
        ArrayList<EventCard> chanceDeck = new ArrayList<>();
        chanceDeck.add(new BonusCard("Scholarship awarded — collect ₪50,000", 50_000));
        chanceDeck.add(new BonusCard("Won the hackathon — collect ₪100,000", 100_000));
        chanceDeck.add(new BonusCard("Research grant approved — collect ₪75,000", 75_000));
        chanceDeck.add(new BonusCard("Tuition reimbursement — collect ₪40,000", 40_000));
        chanceDeck.add(new BonusCard("Dean's list bonus — collect ₪30,000", 30_000));
        chanceDeck.add(new PenaltyCard("Lab equipment fine — pay ₪20,000", 20_000));
        chanceDeck.add(new PenaltyCard("Library late fees — pay ₪10,000", 10_000));
        chanceDeck.add(new PenaltyCard("Parking ticket on campus — pay ₪15,000", 15_000));
        chanceDeck.add(new PenaltyCard("Failed experiment — pay ₪25,000", 25_000));
        chanceDeck.add(new PenaltyCard("Mandatory campus fund — pay ₪50,000", 50_000));

        board = new Board(chanceDeck);
    }

    private void setupPlayers() throws InvalidPlayerCountException {
        String input = JOptionPane.showInputDialog("How many players? (2-4):");
        if (input == null) throw new InvalidPlayerCountException("No input provided.");
        int count;
        try {
            count = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new InvalidPlayerCountException("Enter a number between 2 and 4.");
        }
        if (count < 2 || count > 4)
            throw new InvalidPlayerCountException("Player count must be 2-4, got: " + count);

        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog("Enter name for Player " + (i + 1) + ":");
            if (name == null || name.trim().isEmpty())
                name = "Player " + (i + 1);

            Object[] opts = {"Human", "Bot"};
            int type = JOptionPane.showOptionDialog(null,
                "Is " + name.trim() + " human or bot?", "Player Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);
            players.add(type == 1 ? new BotPlayer(name.trim(), 2_000_000)
                                  : new HumanPlayer(name.trim(), 2_000_000));
        }
    }

    private void playLoop() {
        while (!gameOver) {
            currentRound++;
            for (Player p : players) {
                if (!p.isBankrupt()) {
                    p.takeTurn(board, dice);
                }
                checkGameOver();
                if (gameOver) break;
            }
        }
    }

    private void checkGameOver() {
        long active = players.stream().filter(p -> !p.isBankrupt()).count();
        if (active <= 1) {
            gameOver = true;
        }
    }

    private void showResults() {
        sortByWealth();
        StringBuilder sb = new StringBuilder("=== FINAL RESULTS ===\n\n");
        for (int i = 0; i < players.size(); i++) {
            sb.append((i + 1)).append(". ").append(players.get(i)).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addPlayer(Player p) { players.add(p); }

    public void removePlayer(Player p) { players.remove(p); }

    public void printPlayers() {
        players.forEach(System.out::println);
    }

    // Case-insensitive lookup by name.
    public Player findPlayer(String name) {
        return players.stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    // Sorts by money via Player's Comparable implementation.
    public void sortByWealth() {
        Collections.sort(players);
    }

    // Deep-clones the player list (each Player via its own clone()).
    public ArrayList<Player> clonePlayers() throws CloneNotSupportedException {
        ArrayList<Player> copy = new ArrayList<>();
        for (Player p : players) copy.add(p.clone());
        return copy;
    }
}
