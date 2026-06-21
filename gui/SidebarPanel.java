package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import players.Player;
import tiles.PropertyTile;

public class SidebarPanel extends JPanel {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG      = new Color(18, 18, 42);
    private static final Color CARD_BG = new Color(28, 28, 58);
    private static final Color BORDER  = new Color(55, 55, 100);
    private static final Color TEXT    = new Color(230, 230, 240);
    private static final Color MUTED   = new Color(140, 140, 175);
    private static final Color GOLD    = new Color(255, 210, 50);

    // ── Core widgets ──────────────────────────────────────────────────────────
    private final JPanel   statsPanel;
    private final DicePanel dicePanel;
    private final JButton  rollBtn;

    // Context panels — only one visible at a time
    private final JPanel purchasePanel;
    private final JLabel purchaseTitle;
    private final JButton[] purchaseBtns = new JButton[4]; // levels 0-3
    private final JButton purchaseSkipBtn;

    private final JPanel upgradePanel;
    private final JLabel upgradeTitle;
    private final JLabel upgradeNextLabel;
    private final JButton upgradeConfirmBtn;

    private final JPanel collectPanel;
    private final JLabel collectTileLabel;
    private final JLabel collectDescLabel;

    private final JPanel  stationPanel;
    private final JLabel  stationTitle;

    private final JTextArea logArea;

    private GUIGame game;

    public SidebarPanel() {
        setPreferredSize(new Dimension(330, BoardPanel.BOARD_SIZE + 20));
        setBackground(BG);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Stats ─────────────────────────────────────────────────────────────
        statsPanel = makeCard();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        add(statsPanel, BorderLayout.NORTH);

        // ── Centre (dice + roll + context panels) ─────────────────────────────
        JPanel centre = new JPanel();
        centre.setBackground(BG);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        dicePanel = new DicePanel();
        dicePanel.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(dicePanel);
        centre.add(Box.createVerticalStrut(8));

        rollBtn = makeBtn("ROLL DICE", new Color(55, 120, 190));
        rollBtn.addActionListener(e -> { if (game != null) game.handleRoll(); });
        rollBtn.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(rollBtn);
        centre.add(Box.createVerticalStrut(6));

        // ── Purchase panel ────────────────────────────────────────────────────
        purchasePanel = new JPanel();
        purchasePanel.setBackground(BG);
        purchasePanel.setLayout(new BoxLayout(purchasePanel, BoxLayout.Y_AXIS));
        purchasePanel.setAlignmentX(CENTER_ALIGNMENT);

        purchaseTitle = new JLabel("", SwingConstants.CENTER);
        purchaseTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        purchaseTitle.setForeground(GOLD);
        purchaseTitle.setAlignmentX(CENTER_ALIGNMENT);
        purchasePanel.add(purchaseTitle);
        purchasePanel.add(Box.createVerticalStrut(4));

        String[] lvlLabels = {"Land", "1 House", "2 Houses", "3 Houses"};
        Color[]  lvlColors = {
            new Color(80, 130, 60),
            new Color(45, 155, 75),
            new Color(40, 140, 100),
            new Color(35, 125, 145)
        };
        for (int i = 0; i < 4; i++) {
            final int lvl = i;
            purchaseBtns[i] = makeBtn(lvlLabels[i], lvlColors[i]);
            purchaseBtns[i].addActionListener(e -> { if (game != null) game.handleInitialBuyLevel(lvl); });
            purchaseBtns[i].setAlignmentX(CENTER_ALIGNMENT);
            purchasePanel.add(purchaseBtns[i]);
            purchasePanel.add(Box.createVerticalStrut(3));
        }

        purchaseSkipBtn = makeBtn("Skip — don't buy", new Color(90, 50, 50));
        purchaseSkipBtn.addActionListener(e -> { if (game != null) game.handleInitialBuyLevel(-1); });
        purchaseSkipBtn.setAlignmentX(CENTER_ALIGNMENT);
        purchasePanel.add(purchaseSkipBtn);
        purchasePanel.setVisible(false);
        centre.add(purchasePanel);

        // ── Upgrade panel ─────────────────────────────────────────────────────
        upgradePanel = new JPanel();
        upgradePanel.setBackground(BG);
        upgradePanel.setLayout(new BoxLayout(upgradePanel, BoxLayout.Y_AXIS));
        upgradePanel.setAlignmentX(CENTER_ALIGNMENT);

        upgradeTitle = new JLabel("", SwingConstants.CENTER);
        upgradeTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        upgradeTitle.setForeground(GOLD);
        upgradeTitle.setAlignmentX(CENTER_ALIGNMENT);

        upgradeNextLabel = new JLabel("", SwingConstants.CENTER);
        upgradeNextLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        upgradeNextLabel.setForeground(TEXT);
        upgradeNextLabel.setAlignmentX(CENTER_ALIGNMENT);

        upgradeConfirmBtn = makeBtn("Upgrade", new Color(45, 155, 75));
        upgradeConfirmBtn.addActionListener(e -> { if (game != null) game.handleUpgradeDecision(true); });
        upgradeConfirmBtn.setAlignmentX(CENTER_ALIGNMENT);

        JButton upgradeKeepBtn = makeBtn("Keep as-is", new Color(70, 70, 100));
        upgradeKeepBtn.addActionListener(e -> { if (game != null) game.handleUpgradeDecision(false); });
        upgradeKeepBtn.setAlignmentX(CENTER_ALIGNMENT);

        upgradePanel.add(upgradeTitle);
        upgradePanel.add(Box.createVerticalStrut(3));
        upgradePanel.add(upgradeNextLabel);
        upgradePanel.add(Box.createVerticalStrut(5));
        upgradePanel.add(upgradeConfirmBtn);
        upgradePanel.add(Box.createVerticalStrut(3));
        upgradePanel.add(upgradeKeepBtn);
        upgradePanel.setVisible(false);
        centre.add(upgradePanel);

        // ── Collect panel ─────────────────────────────────────────────────────
        collectPanel = new JPanel();
        collectPanel.setBackground(CARD_BG);
        collectPanel.setLayout(new BoxLayout(collectPanel, BoxLayout.Y_AXIS));
        collectPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        collectPanel.setAlignmentX(CENTER_ALIGNMENT);

        collectTileLabel = new JLabel("", SwingConstants.CENTER);
        collectTileLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        collectTileLabel.setForeground(GOLD);
        collectTileLabel.setAlignmentX(CENTER_ALIGNMENT);

        collectDescLabel = new JLabel("", SwingConstants.CENTER);
        collectDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        collectDescLabel.setForeground(TEXT);
        collectDescLabel.setAlignmentX(CENTER_ALIGNMENT);

        JButton collectBtn = makeBtn("Collect", new Color(55, 120, 190));
        collectBtn.addActionListener(e -> { if (game != null) game.handleCollect(); });
        collectBtn.setAlignmentX(CENTER_ALIGNMENT);

        collectPanel.add(collectTileLabel);
        collectPanel.add(Box.createVerticalStrut(4));
        collectPanel.add(collectDescLabel);
        collectPanel.add(Box.createVerticalStrut(6));
        collectPanel.add(collectBtn);
        collectPanel.setVisible(false);
        centre.add(Box.createVerticalStrut(3));
        centre.add(collectPanel);

        // ── Station buy panel ─────────────────────────────────────────────────
        stationPanel = new JPanel();
        stationPanel.setBackground(CARD_BG);
        stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
        stationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 150, 210)),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        stationPanel.setAlignmentX(CENTER_ALIGNMENT);

        stationTitle = new JLabel("", SwingConstants.CENTER);
        stationTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        stationTitle.setForeground(GOLD);
        stationTitle.setAlignmentX(CENTER_ALIGNMENT);

        JButton stationBuyBtn = makeBtn("Buy  ₪200,000", new Color(45, 130, 195));
        stationBuyBtn.addActionListener(e -> { if (game != null) game.handleStationBuy(true); });
        stationBuyBtn.setAlignmentX(CENTER_ALIGNMENT);

        JButton stationSkipBtn = makeBtn("Skip — don't buy", new Color(90, 50, 50));
        stationSkipBtn.addActionListener(e -> { if (game != null) game.handleStationBuy(false); });
        stationSkipBtn.setAlignmentX(CENTER_ALIGNMENT);

        stationPanel.add(stationTitle);
        stationPanel.add(Box.createVerticalStrut(6));
        stationPanel.add(stationBuyBtn);
        stationPanel.add(Box.createVerticalStrut(3));
        stationPanel.add(stationSkipBtn);
        stationPanel.setVisible(false);
        centre.add(Box.createVerticalStrut(3));
        centre.add(stationPanel);

        add(centre, BorderLayout.CENTER);

        // ── Log panel ─────────────────────────────────────────────────────────
        JPanel logCard = makeCard();
        logCard.setPreferredSize(new Dimension(310, 175));
        logCard.setLayout(new BorderLayout(0, 4));

        JLabel logTitle = new JLabel("GAME LOG");
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        logTitle.setForeground(GOLD);
        logCard.add(logTitle, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(12, 12, 30));
        logArea.setForeground(new Color(195, 195, 225));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        logCard.add(scroll, BorderLayout.CENTER);

        add(logCard, BorderLayout.SOUTH);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setGame(GUIGame g) { this.game = g; }

    public void updateDisplay(ArrayList<Player> players, Player current, int round) {
        statsPanel.removeAll();

        JLabel header = new JLabel("ROUND " + round
            + (game != null ? " / " + game.getMaxRounds() : ""));
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setForeground(GOLD);
        header.setAlignmentX(LEFT_ALIGNMENT);
        statsPanel.add(header);
        statsPanel.add(Box.createVerticalStrut(5));

        for (int i = 0; i < players.size(); i++) {
            Player p   = players.get(i);
            boolean hi = p.equals(current) && !p.isBankrupt();
            final int fi = i;

            // ── Player summary row ────────────────────────────────────────────
            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.setBackground(hi ? new Color(45, 45, 90) : CARD_BG);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hi ? new Color(100, 130, 205) : BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    ((Graphics2D) g).setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(BoardPanel.PLAYER_COLORS[fi % BoardPanel.PLAYER_COLORS.length]);
                    g.fillOval(1, 1, 15, 15);
                }
            };
            dot.setPreferredSize(new Dimension(17, 17));
            dot.setOpaque(false);
            row.add(dot, BorderLayout.WEST);

            JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
            info.setOpaque(false);

            String jailTag = p.isInJail() ? "  [JAIL " + p.getJailTurnsLeft() + "]" : "";
            JLabel nameL = new JLabel(p.getName()
                + (p.isBankrupt() ? "  [OUT]" : jailTag));
            nameL.setFont(new Font("SansSerif", Font.BOLD, 11));
            nameL.setForeground(p.isBankrupt() ? MUTED : (p.isInJail() ? new Color(255, 160, 50) : TEXT));

            JLabel statsL = new JLabel(String.format("₪%,d  pos:%d  props:%d",
                p.getMoney(), p.getPosition(), p.getOwnedProperties().size()));
            statsL.setFont(new Font("SansSerif", Font.PLAIN, 10));
            statsL.setForeground(MUTED);

            info.add(nameL);
            info.add(statsL);
            row.add(info, BorderLayout.CENTER);
            statsPanel.add(row);

            // ── Property list under the player row ────────────────────────────
            java.util.ArrayList<tiles.PropertyTile> props = p.getOwnedProperties();
            for (tiles.PropertyTile pt : props) {
                final java.awt.Color propColor = pt.getGroupColor();
                JPanel propRow = new JPanel(new BorderLayout(4, 0));
                propRow.setBackground(new Color(28, 28, 50));
                propRow.setBorder(BorderFactory.createEmptyBorder(1, 22, 1, 4));
                propRow.setAlignmentX(LEFT_ALIGNMENT);
                propRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

                // color swatch
                JPanel swatch = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (propColor != null) {
                            g.setColor(propColor);
                            g.fillRect(0, 2, 10, 10);
                        }
                    }
                };
                swatch.setPreferredSize(new Dimension(12, 14));
                swatch.setOpaque(false);
                propRow.add(swatch, BorderLayout.WEST);

                String lvl = pt.getLevel() == 0 ? "L"
                    : pt.getLevel() == 4 ? "H" : pt.getLevel() + "H";
                JLabel propLabel = new JLabel(
                    truncate(pt.getName(), 14) + "  " + lvl + "  ₪" + pt.getRent());
                propLabel.setFont(new Font("Monospaced", Font.PLAIN, 9));
                propLabel.setForeground(new Color(170, 200, 170));
                propRow.add(propLabel, BorderLayout.CENTER);

                statsPanel.add(propRow);
            }

            if (i < players.size() - 1) statsPanel.add(Box.createVerticalStrut(3));
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    public void setDice(int d1, int d2) {
        dicePanel.setValues(d1, d2);
        dicePanel.repaint();
    }

    public void setRollEnabled(boolean on) {
        rollBtn.setEnabled(on);
        rollBtn.setBackground(on ? new Color(55, 120, 190) : new Color(50, 50, 75));
    }

    public void showPurchasePanel(PropertyTile pt, boolean canBuy3, int playerMoney) {
        purchaseTitle.setText(pt.getName() + " — For Sale  (you have ₪" + playerMoney + ")");
        int[] rents = pt.getRentByLevel();
        String[] labels = {"Land", "1 House", "2 Houses", "3 Houses"};
        Color[] lvlColors = {
            new Color(80, 130, 60), new Color(45, 155, 75),
            new Color(40, 140, 100), new Color(35, 125, 145)
        };
        for (int i = 0; i < 4; i++) {
            int cost = pt.getPurchaseCost(i);
            boolean canAfford = playerMoney >= cost;
            boolean available = canAfford && (i < 3 || canBuy3);
            purchaseBtns[i].setText(labels[i] + "  —  ₪" + cost + "  (rent ₪" + rents[i] + ")");
            purchaseBtns[i].setEnabled(available);
            purchaseBtns[i].setBackground(available ? lvlColors[i] : new Color(50, 50, 75));
        }
        if (!canBuy3 && playerMoney >= pt.getPurchaseCost(3)) {
            purchaseBtns[3].setText("[LOCKED] 3 Houses — finish lap 1 first");
        }
        hideAll();
        purchasePanel.setVisible(true);
        revalidate();
    }

    public void hidePurchasePanel() {
        purchasePanel.setVisible(false);
        revalidate();
    }

    public void showUpgradePanel(PropertyTile pt, boolean canBuy3) {
        upgradeTitle.setText(pt.getName() + "  (" + pt.getLevelName() + ")");
        boolean locked = pt.getLevel() == 2 && !canBuy3;
        if (locked) {
            upgradeNextLabel.setText("3 Houses locked — finish lap 1 first");
            upgradeConfirmBtn.setEnabled(false);
            upgradeConfirmBtn.setBackground(new Color(50, 50, 75));
        } else {
            int cost    = pt.getUpgradeCost();
            int newRent = pt.getRentByLevel()[pt.getLevel() + 1];
            upgradeNextLabel.setText("→ " + pt.getNextLevelName()
                + "  ₪" + cost + "  (rent ₪" + newRent + ")");
            upgradeConfirmBtn.setEnabled(true);
            upgradeConfirmBtn.setBackground(new Color(45, 155, 75));
        }
        hideAll();
        upgradePanel.setVisible(true);
        revalidate();
    }

    public void hideUpgradePanel() {
        upgradePanel.setVisible(false);
        revalidate();
    }

    public void showCollectPanel(String tileName, String desc) {
        collectTileLabel.setText(tileName);
        collectDescLabel.setText("<html><center>" + desc + "</center></html>");
        hideAll();
        collectPanel.setVisible(true);
        revalidate();
    }

    public void hideCollectPanel() {
        collectPanel.setVisible(false);
        revalidate();
    }

    public void addLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void showGameOver() {
        rollBtn.setEnabled(false);
        rollBtn.setText("GAME OVER");
        rollBtn.setBackground(new Color(80, 50, 50));
        hideAll();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private void hideAll() {
        purchasePanel.setVisible(false);
        upgradePanel.setVisible(false);
        collectPanel.setVisible(false);
        stationPanel.setVisible(false);
    }

    public void showStationPanel(tiles.ShuttleStationTile st, int playerMoney) {
        stationTitle.setText("<html><center>" + st.getName()
            + "<br><span style='font-size:9px;color:#aaaacc'>For Sale — ₪200,000 &nbsp;|&nbsp; You have ₪"
            + String.format("%,d", playerMoney) + "</span></center></html>");
        hideAll();
        boolean canAfford = playerMoney >= tiles.ShuttleStationTile.getPrice();
        stationPanel.getComponent(2).setEnabled(canAfford); // Buy button
        stationPanel.setVisible(true);
        revalidate();
    }

    public void hideStationPanel() {
        stationPanel.setVisible(false);
        revalidate();
    }

    private JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(7, 7, 7, 7)));
        return p;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setPreferredSize(new Dimension(305, 34));
        return b;
    }

    // ── Dice panel ────────────────────────────────────────────────────────────

    static class DicePanel extends JPanel {
        private int d1 = 0, d2 = 0;

        DicePanel() {
            setPreferredSize(new Dimension(310, 95));
            setBackground(new Color(18, 18, 42));
        }

        void setValues(int a, int b) { d1 = a; d2 = b; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (d1 == 0) {
                g2.setColor(new Color(90, 90, 130));
                g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                FontMetrics fm = g2.getFontMetrics();
                String hint = "Press ROLL DICE to start";
                g2.drawString(hint, (getWidth() - fm.stringWidth(hint)) / 2, getHeight() / 2 + 5);
                return;
            }

            int sz = 68, gap = 16;
            int x0 = (getWidth() - (sz * 2 + gap)) / 2;
            int y0 = (getHeight() - sz) / 2 - 6;

            drawDie(g2, x0,       y0, sz, d1);
            drawDie(g2, x0+sz+gap, y0, sz, d2);

            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(new Color(200, 200, 240));
            String sum = "= " + (d1 + d2);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(sum, (getWidth() - fm.stringWidth(sum)) / 2, y0 + sz + 16);
        }

        private void drawDie(Graphics2D g2, int x, int y, int s, int val) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(x+4, y+4, s, s, 12, 12));

            g2.setPaint(new GradientPaint(x, y, Color.WHITE, x+s, y+s, new Color(228, 228, 228)));
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(x, y, s, s, 12, 12));

            g2.setPaint(new Color(160, 160, 160));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(x, y, s, s, 12, 12));

            g2.setColor(new Color(30, 30, 30));
            int r = 6, m = s / 4;
            int cx = x+s/2, cy = y+s/2, x1=x+m, x2=x+s-m, y1=y+m, y2=y+s-m;
            int[][] dots;
            if      (val==1) dots=new int[][]{{cx,cy}};
            else if (val==2) dots=new int[][]{{x1,y1},{x2,y2}};
            else if (val==3) dots=new int[][]{{x1,y1},{cx,cy},{x2,y2}};
            else if (val==4) dots=new int[][]{{x1,y1},{x2,y1},{x1,y2},{x2,y2}};
            else if (val==5) dots=new int[][]{{x1,y1},{x2,y1},{cx,cy},{x1,y2},{x2,y2}};
            else             dots=new int[][]{{x1,y1},{x2,y1},{x1,cy},{x2,cy},{x1,y2},{x2,y2}};
            for (int[] d : dots) g2.fillOval(d[0]-r/2, d[1]-r/2, r, r);
        }
    }
}
