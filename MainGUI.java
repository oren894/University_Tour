import gui.GameWindow;
import javax.swing.SwingUtilities;

// Entry point for the full Swing GUI version of the game.
public class MainGUI {
    public static void main(String[] args) {
        // Swing components must be created/updated on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
