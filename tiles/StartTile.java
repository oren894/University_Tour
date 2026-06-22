package tiles;

import java.util.Random;
import javax.swing.JOptionPane;
import players.Player;
import players.HumanPlayer;
import exceptions.InsufficientFundsException;

public class StartTile implements Tile {

    private static final boolean[] IS_RED = {
        false, // 0 green
        true,  false, true,  false, true,  false, true,  false, true,  false, // 1-10
        false, true,  false, true,  false, true,  false, true,  true,  false, // 11-20
        true,  false, true,  false, true,  false, true,  false, false, true,  // 21-30
        false, true,  false, true,  false, true                                // 31-36
    };

    @Override
    public String getName() { return "Start"; }

    @Override
    public void landOn(Player player) {
        player.receive(200_000);
        if (player instanceof HumanPlayer) {
            JOptionPane.showMessageDialog(null,
                "You landed on Start! Collect ₪200,000!\n"
                + "Balance: ₪" + String.format("%,d", player.getMoney())
                + "\n\nWelcome to the Casino!",
                "Start — Casino", JOptionPane.INFORMATION_MESSAGE);
            playRoulette((HumanPlayer) player);
        }
    }

    private void playRoulette(HumanPlayer player) {
        String[] betTypes = {
            "Red (1:1)", "Black (1:1)",
            "Odd (1:1)", "Even (1:1)",
            "Low 1-18 (1:1)", "High 19-36 (1:1)",
            "1st Dozen 1-12 (2:1)", "2nd Dozen 13-24 (2:1)", "3rd Dozen 25-36 (2:1)",
            "Single Number (35:1)",
            "Skip — no bet"
        };

        Object betChoice = JOptionPane.showInputDialog(null,
            player.getName() + " — Roulette!\nBalance: ₪" + String.format("%,d", player.getMoney())
            + "\n\nChoose your bet type:",
            "Roulette", JOptionPane.QUESTION_MESSAGE,
            null, betTypes, betTypes[0]);

        if (betChoice == null || betChoice.toString().startsWith("Skip")) return;

        int singleTarget = -1;
        if (betChoice.toString().startsWith("Single")) {
            String[] numbers = new String[37];
            for (int i = 0; i <= 36; i++) numbers[i] = String.valueOf(i);
            Object numChoice = JOptionPane.showInputDialog(null,
                "Choose a number (0–36):",
                "Single Number", JOptionPane.QUESTION_MESSAGE,
                null, numbers, "0");
            if (numChoice == null) return;
            singleTarget = Integer.parseInt(numChoice.toString());
        }

        String amountStr = JOptionPane.showInputDialog(null,
            "Balance: ₪" + String.format("%,d", player.getMoney())
            + "\nEnter bet amount (1 – " + String.format("%,d", player.getMoney()) + "):",
            "Bet Amount");
        if (amountStr == null) return;

        int betAmount;
        try {
            betAmount = Integer.parseInt(amountStr.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid amount — bet cancelled.");
            return;
        }
        if (betAmount <= 0 || betAmount > player.getMoney()) {
            JOptionPane.showMessageDialog(null, "Invalid amount — bet cancelled.");
            return;
        }

        int result = new Random().nextInt(37);
        String color  = result == 0 ? "Green" : (IS_RED[result] ? "Red" : "Black");
        String parity = result == 0 ? "—" : (result % 2 == 1 ? "Odd" : "Even");

        boolean won = false;
        int payout = 0;
        String bet = betChoice.toString();
        if      (bet.startsWith("Red"))    { won = result != 0 && IS_RED[result];   payout = betAmount; }
        else if (bet.startsWith("Black"))  { won = result != 0 && !IS_RED[result];  payout = betAmount; }
        else if (bet.startsWith("Odd"))    { won = result != 0 && result % 2 == 1;  payout = betAmount; }
        else if (bet.startsWith("Even"))   { won = result != 0 && result % 2 == 0;  payout = betAmount; }
        else if (bet.startsWith("Low"))    { won = result >= 1  && result <= 18;     payout = betAmount; }
        else if (bet.startsWith("High"))   { won = result >= 19 && result <= 36;     payout = betAmount; }
        else if (bet.startsWith("1st"))    { won = result >= 1  && result <= 12;     payout = betAmount * 2; }
        else if (bet.startsWith("2nd"))    { won = result >= 13 && result <= 24;     payout = betAmount * 2; }
        else if (bet.startsWith("3rd"))    { won = result >= 25 && result <= 36;     payout = betAmount * 2; }
        else if (bet.startsWith("Single")) { won = result == singleTarget;           payout = betAmount * 35; }

        String msg = "Ball landed on: " + result + " (" + color + ", " + parity + ")\n\n";
        if (won) {
            player.receive(payout);
            msg += "YOU WIN! +" + String.format("%,d", payout)
                + "\nBalance: ₪" + String.format("%,d", player.getMoney());
        } else {
            try { player.pay(betAmount); }
            catch (InsufficientFundsException e) { player.setBankrupt(true); }
            msg += "YOU LOSE! -₪" + String.format("%,d", betAmount)
                + "\nBalance: ₪" + String.format("%,d", player.getMoney());
        }

        JOptionPane.showMessageDialog(null, msg, "Roulette Result",
            won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }
}
