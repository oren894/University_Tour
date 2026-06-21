package players;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import game.Board;
import game.Dice;

public class HumanPlayer extends Player {

    public HumanPlayer(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.ownedProperties = new ArrayList<>();
        this.ownedStations = new ArrayList<>();
    }

    @Override
    public void takeTurn(Board board, Dice dice) {
        int choice = JOptionPane.showConfirmDialog(null,
            name + "'s turn!\nMoney: ₪" + money + "\nPosition: " + position
            + "\n\nPress OK to roll the dice.",
            "Your Turn", JOptionPane.OK_CANCEL_OPTION);

        if (choice != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        int roll = dice.roll();
        JOptionPane.showMessageDialog(null,
            "You rolled " + roll + "!\nMoving to position " + ((position + roll) % 36)
            + " — " + board.getTile((position + roll) % 36).getName(),
            "Dice Roll", JOptionPane.INFORMATION_MESSAGE);

        move(roll);
        board.getTile(position).landOn(this);
    }
}
