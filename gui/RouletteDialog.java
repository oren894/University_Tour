package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import players.Player;

public class RouletteDialog extends JDialog {

    private static final boolean[] IS_RED = new boolean[37];
    static {
        for (int n : new int[]{1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36})
            IS_RED[n] = true;
    }

    // European roulette pocket order (clockwise from 0)
    private static final int[] WHEEL_ORDER = {
        0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,
        5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26
    };

    private static final Color BG      = new Color(18, 18, 42);
    private static final Color CARD_BG = new Color(28, 28, 58);
    private static final Color GOLD    = new Color(255, 210, 50);
    private static final Color TEXT    = new Color(230, 230, 240);
    private static final Color MUTED   = new Color(140, 140, 175);

    private final Player player;
    private int netResult = 0;

    private double  wheelAngle = 0;
    private int     winNumber  = -1;
    private boolean spinning   = false;
    private boolean done       = false;
    private Timer   spinTimer;

    private String             betType   = null;
    private int                betNumber = 0;
    private int                betAmount = 0;

    private final ButtonGroup  betGroup = new ButtonGroup();
    private final WheelPanel   wheelPanel;
    private JComboBox<Integer> numberBox;
    private JLabel             betAmountLabel;
    private JSlider            betSlider;
    private JButton            spinBtn;
    private JLabel             resultLabel;
    private JButton            collectBtn;

    public RouletteDialog(JFrame owner, Player player) {
        super(owner, "Casino Roulette", true);
        this.player = player;

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 0));

        JLabel title = new JLabel("CASINO  ROULETTE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(GOLD);
        title.setOpaque(true);
        title.setBackground(BG);
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        wheelPanel = new WheelPanel();
        add(wheelPanel, BorderLayout.WEST);

        add(buildControls(), BorderLayout.CENTER);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        resultLabel.setForeground(GOLD);
        resultLabel.setOpaque(true);
        resultLabel.setBackground(new Color(10, 10, 30));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(resultLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    private JPanel buildControls() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        p.setPreferredSize(new Dimension(290, 390));

        p.add(label("PLACE YOUR BET", true, GOLD));
        p.add(Box.createVerticalStrut(8));

        p.add(label("1 : 1  — Even Chances", false, MUTED));
        p.add(Box.createVerticalStrut(3));
        p.add(row2(tog("Red",      "RED",   new Color(180,30,30)),
                   tog("Black",    "BLACK",  new Color(30,30,30))));
        p.add(Box.createVerticalStrut(3));
        p.add(row2(tog("Odd",      "ODD",   new Color(80,55,120)),
                   tog("Even",     "EVEN",   new Color(55,80,120))));
        p.add(Box.createVerticalStrut(3));
        p.add(row2(tog("1 – 18",   "LOW",   new Color(50,100,70)),
                   tog("19 – 36",  "HIGH",   new Color(70,100,50))));
        p.add(Box.createVerticalStrut(8));

        p.add(label("2 : 1  — Dozens", false, MUTED));
        p.add(Box.createVerticalStrut(3));
        p.add(row3(tog("1st 12", "D1", new Color(100,70,25)),
                   tog("2nd 12", "D2", new Color(80,80,25)),
                   tog("3rd 12", "D3", new Color(60,90,25))));
        p.add(Box.createVerticalStrut(8));

        p.add(label("35 : 1  — Single Number", false, MUTED));
        p.add(Box.createVerticalStrut(3));
        JPanel numRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        numRow.setBackground(BG);
        numRow.setAlignmentX(LEFT_ALIGNMENT);
        Integer[] nums = new Integer[37];
        for (int i = 0; i < 37; i++) nums[i] = i;
        numberBox = new JComboBox<>(nums);
        numberBox.setPreferredSize(new Dimension(65, 28));
        numberBox.setBackground(CARD_BG);
        numberBox.setForeground(TEXT);
        numberBox.addActionListener(e -> {
            if ("NUM".equals(betType)) betNumber = (Integer) numberBox.getSelectedItem();
        });
        JToggleButton numBtn = tog("Number:", "NUM", new Color(70,40,100));
        numBtn.addActionListener(e -> betNumber = (Integer) numberBox.getSelectedItem());
        numRow.add(numBtn);
        numRow.add(numberBox);
        p.add(numRow);
        p.add(Box.createVerticalStrut(12));

        // Slider
        p.add(label("BET AMOUNT", true, GOLD));
        p.add(Box.createVerticalStrut(4));
        betAmountLabel = new JLabel("₪0");
        betAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        betAmountLabel.setForeground(GOLD);
        betAmountLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(betAmountLabel);

        betSlider = new JSlider(0, Math.max(1, player.getMoney()), 0);
        betSlider.setBackground(BG);
        betSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        betSlider.setAlignmentX(LEFT_ALIGNMENT);
        betSlider.addChangeListener(e -> {
            betAmount = betSlider.getValue();
            betAmountLabel.setText("₪" + String.format("%,d", betAmount));
            updateSpinBtn();
        });
        p.add(betSlider);
        p.add(Box.createVerticalStrut(10));

        spinBtn = btn("SPIN THE WHEEL", new Color(180, 30, 30), 16);
        spinBtn.setEnabled(false);
        spinBtn.addActionListener(e -> startSpin());
        p.add(spinBtn);
        p.add(Box.createVerticalStrut(6));

        collectBtn = btn("COLLECT & CONTINUE", new Color(35, 120, 55), 13);
        collectBtn.setVisible(false);
        collectBtn.addActionListener(e -> dispose());
        p.add(collectBtn);

        return p;
    }

    // ── Spin logic ────────────────────────────────────────────────────────────

    private void startSpin() {
        spinning = true;
        done     = false;
        collectBtn.setVisible(false);
        resultLabel.setForeground(MUTED);
        resultLabel.setText("Spinning...");
        spinBtn.setEnabled(false);

        winNumber = new Random().nextInt(37);

        int winIdx = 0;
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == winNumber) { winIdx = i; break; }
        }

        double sectorDeg = 360.0 / WHEEL_ORDER.length;
        // Rotate so that winIdx sector centres under the top pointer
        // Sector i midpoint is at: -90 + effAngle + i*sectorDeg + sectorDeg/2
        // We want that to equal -90 (top), so: effAngle = -(i*sectorDeg + sectorDeg/2) mod 360
        double targetAngle = ((-(winIdx * sectorDeg + sectorDeg / 2)) % 360 + 360) % 360;
        double currentMod  = wheelAngle % 360;
        double delta       = (targetAngle - currentMod + 360) % 360;
        double totalSpin   = 360 * 6 + delta; // 6 full rotations + final alignment

        double startAngle = wheelAngle;
        final int FRAMES = 120; // 120 × 30ms = 3.6 s
        final int[] frame = {0};

        spinTimer = new Timer(30, null);
        spinTimer.addActionListener(ev -> {
            frame[0]++;
            if (frame[0] >= FRAMES) {
                spinTimer.stop();
                wheelAngle = startAngle + totalSpin;
                spinning   = false;
                done       = true;
                wheelPanel.repaint();
                showResult();
            } else {
                double t = (double) frame[0] / FRAMES;
                wheelAngle = startAngle + totalSpin * (1 - Math.pow(1 - t, 3));
                wheelPanel.repaint();
            }
        });
        spinTimer.start();
    }

    private void showResult() {
        boolean won;
        int payout;
        switch (betType) {
            case "RED":   won = winNumber > 0 && IS_RED[winNumber];             payout = 1;  break;
            case "BLACK": won = winNumber > 0 && !IS_RED[winNumber];            payout = 1;  break;
            case "ODD":   won = winNumber > 0 && winNumber % 2 == 1;           payout = 1;  break;
            case "EVEN":  won = winNumber > 0 && winNumber % 2 == 0;           payout = 1;  break;
            case "LOW":   won = winNumber >= 1  && winNumber <= 18;             payout = 1;  break;
            case "HIGH":  won = winNumber >= 19 && winNumber <= 36;             payout = 1;  break;
            case "D1":    won = winNumber >= 1  && winNumber <= 12;             payout = 2;  break;
            case "D2":    won = winNumber >= 13 && winNumber <= 24;             payout = 2;  break;
            case "D3":    won = winNumber >= 25 && winNumber <= 36;             payout = 2;  break;
            case "NUM":   won = winNumber == betNumber;                          payout = 35; break;
            default:      won = false; payout = 0;
        }

        String col = winNumber == 0 ? "GREEN" : (IS_RED[winNumber] ? "RED" : "BLACK");
        if (won) {
            netResult = betAmount * payout;
            resultLabel.setForeground(new Color(50, 220, 80));
            resultLabel.setText(winNumber + "  " + col + "  —  WON  ₪" + String.format("%,d", netResult) + "!");
        } else {
            netResult = -betAmount;
            resultLabel.setForeground(new Color(220, 60, 60));
            resultLabel.setText(winNumber + "  " + col + "  —  LOST  ₪" + String.format("%,d", betAmount));
        }
        collectBtn.setVisible(true);
        revalidate();
    }

    public int getNetResult() { return netResult; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateSpinBtn() {
        spinBtn.setEnabled(betType != null && betAmount > 0 && !spinning && !done);
    }

    private JLabel label(String text, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, bold ? 12 : 10));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel row2(JToggleButton a, JToggleButton b) {
        JPanel r = new JPanel(new GridLayout(1, 2, 4, 0));
        r.setBackground(BG);
        r.setAlignmentX(LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        r.add(a); r.add(b);
        return r;
    }

    private JPanel row3(JToggleButton a, JToggleButton b, JToggleButton c) {
        JPanel r = new JPanel(new GridLayout(1, 3, 4, 0));
        r.setBackground(BG);
        r.setAlignmentX(LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        r.add(a); r.add(b); r.add(c);
        return r;
    }

    private JToggleButton tog(String label, String type, Color bg) {
        JToggleButton b = new JToggleButton(label);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 120), 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        betGroup.add(b);
        b.addActionListener(e -> { betType = type; updateSpinBtn(); });
        return b;
    }

    private JButton btn(String text, Color bg, int fontSize) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setAlignmentX(LEFT_ALIGNMENT);
        return b;
    }

    // ── Animated wheel ────────────────────────────────────────────────────────

    private class WheelPanel extends JPanel {
        WheelPanel() {
            setPreferredSize(new Dimension(310, 390));
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int r  = Math.min(cx, cy) - 18;
            double sectorDeg = 360.0 / WHEEL_ORDER.length;
            double effAngle  = wheelAngle % 360;

            // Outer gold rim
            g2.setColor(new Color(200, 180, 100));
            g2.setStroke(new BasicStroke(7));
            g2.drawOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);

            // Sectors
            for (int i = 0; i < WHEEL_ORDER.length; i++) {
                int num = WHEEL_ORDER[i];
                double startAng = -90 + effAngle + i * sectorDeg;

                Color fill;
                if (num == 0)         fill = new Color(0, 140, 0);
                else if (IS_RED[num]) fill = new Color(200, 30, 30);
                else                  fill = new Color(18, 18, 18);

                if (done && num == winNumber) fill = fill.brighter().brighter();

                g2.setColor(fill);
                g2.fill(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2,
                    startAng, sectorDeg + 0.3, Arc2D.PIE));

                g2.setColor(new Color(200, 180, 100, 100));
                g2.setStroke(new BasicStroke(0.5f));
                g2.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2,
                    startAng, sectorDeg, Arc2D.PIE));

                double midRad = Math.toRadians(startAng + sectorDeg / 2);
                int tx = (int)(cx + r * 0.74 * Math.cos(midRad));
                int ty = (int)(cy + r * 0.74 * Math.sin(midRad));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, r > 100 ? 9 : 7));
                FontMetrics fm = g2.getFontMetrics();
                String ns = String.valueOf(num);
                g2.drawString(ns, tx - fm.stringWidth(ns) / 2, ty + fm.getAscent() / 2 - 1);
            }

            // Center hub
            int hr = r / 5;
            g2.setColor(new Color(18, 18, 42));
            g2.fillOval(cx - hr, cy - hr, hr * 2, hr * 2);
            g2.setColor(new Color(200, 180, 100));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(cx - hr, cy - hr, hr * 2, hr * 2);

            // Golden pointer at top
            g2.setColor(new Color(255, 215, 0));
            int[] px = {cx - 10, cx + 10, cx};
            int[] py = {cy - r + 2, cy - r + 2, cy - r + 20};
            g2.fillPolygon(px, py, 3);
            g2.setColor(new Color(160, 120, 0));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawPolygon(px, py, 3);

            // Win number overlay in center hub
            if (done && winNumber >= 0) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String ws = String.valueOf(winNumber);
                g2.setColor(GOLD);
                g2.drawString(ws, cx - fm.stringWidth(ws) / 2, cy + fm.getAscent() / 2 - 2);
            }
        }
    }
}
