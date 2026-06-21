package gui;

import javax.swing.*;
import java.awt.*;
import players.Player;
import tiles.PropertyTile;

public class GameWindow extends JFrame {

    private final BoardPanel   boardPanel;
    private final SidebarPanel sidePanel;
    private GUIGame game;

    public GameWindow() {
        setTitle("Business Tour");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new BoardPanel();
        sidePanel  = new SidebarPanel();

        JPanel root = new JPanel(new BorderLayout(12, 0));
        root.setBackground(new Color(15, 15, 30));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(boardPanel, BorderLayout.CENTER);
        root.add(sidePanel,  BorderLayout.EAST);

        add(root);
        pack();
        setLocationRelativeTo(null);

        game = new GUIGame(this);
        sidePanel.setGame(game);

        SwingUtilities.invokeLater(() -> game.start());
    }

    // ── Called by GUIGame to refresh UI ──────────────────────────────────────

    public void updateBoard() {
        boardPanel.setPlayers(game.getPlayers());
        boardPanel.setBuildingLevels(game.getBuildingLevels());
        boardPanel.setTileOwnerIndex(game.getTileOwnerIndex());
        boardPanel.setTileRent(game.getTileRent());
        boardPanel.setWCTileIndex(game.getWCTileIndex());
        boardPanel.repaint();
    }

    public void updateSidebar() {
        sidePanel.updateDisplay(game.getPlayers(), game.getCurrentPlayer(), game.getCurrentRound());
    }

    public void setDiceResult(int d1, int d2) {
        sidePanel.setDice(d1, d2);
    }

    public void log(String message) {
        sidePanel.addLog(message);
    }

    public void setRollEnabled(boolean on) {
        sidePanel.setRollEnabled(on);
    }

    public void showGameOver() {
        sidePanel.showGameOver();
    }

    // ── Purchase (initial buy) ────────────────────────────────────────────────

    public void showInitialBuyOffer(PropertyTile pt, boolean completedFirstLap, int playerMoney) {
        sidePanel.showPurchasePanel(pt, completedFirstLap, playerMoney);
    }

    public void hidePurchasePanel() {
        sidePanel.hidePurchasePanel();
    }

    // ── Upgrade ───────────────────────────────────────────────────────────────

    public void showUpgradeOffer(PropertyTile pt, boolean completedFirstLap) {
        sidePanel.showUpgradePanel(pt, completedFirstLap);
    }

    public void hideUpgradePanel() {
        sidePanel.hideUpgradePanel();
    }

    // ── Shuttle station buy ───────────────────────────────────────────────────

    public void showStationBuyOffer(tiles.ShuttleStationTile st, int playerMoney) {
        sidePanel.showStationPanel(st, playerMoney);
    }

    public void hideStationPanel() {
        sidePanel.hideStationPanel();
    }

    // ── Collect card ──────────────────────────────────────────────────────────

    public void showCollectCard(String tileName, String cardDesc) {
        sidePanel.showCollectPanel(tileName, cardDesc);
    }

    public void hideCollectPanel() {
        sidePanel.hideCollectPanel();
    }
}
