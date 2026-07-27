package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import players.Player;
import tiles.PropertyTile;
import tiles.ShuttleStationTile;

public class SellPropertiesDialog extends JDialog {

    private static final Color BG      = new Color(18, 18, 42);
    private static final Color GOLD    = new Color(255, 210, 50);
    private static final Color TEXT    = new Color(230, 230, 240);
    private static final Color MUTED   = new Color(140, 140, 175);
    private static final Color GREEN   = new Color(50, 200, 80);
    private static final Color RED_COL = new Color(220, 60, 60);

    private final Player player;
    private final int    amountNeeded;

    private JLabel  headerLabel;
    private JLabel  selectedLabel;
    private JLabel  projectedLabel;
    private JButton sellBtn;

    private final ArrayList<JCheckBox>          propBoxes    = new ArrayList<>();
    private final ArrayList<PropertyTile>       propTiles    = new ArrayList<>();
    private final ArrayList<JCheckBox>          stationBoxes = new ArrayList<>();
    private final ArrayList<ShuttleStationTile> stations     = new ArrayList<>();

    public SellPropertiesDialog(JFrame owner, Player player, int amountNeeded) {
        super(owner, "Sell Properties to Pay Debt", true);
        this.player       = player;
        this.amountNeeded = amountNeeded;

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(new Color(10, 10, 30));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("SELL PROPERTIES TO PAY DEBT");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(GOLD);
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(5));

        headerLabel = new JLabel();
        headerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        headerLabel.setForeground(TEXT);
        headerLabel.setAlignmentX(LEFT_ALIGNMENT);
        header.add(headerLabel);
        add(header, BorderLayout.NORTH);

        // ── Scrollable property list ──────────────────────────────────────────
        JPanel listPanel = new JPanel();
        listPanel.setBackground(BG);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        if (!player.getOwnedProperties().isEmpty()) {
            JLabel propTitle = new JLabel("Properties  (75% of purchase value)");
            propTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
            propTitle.setForeground(MUTED);
            propTitle.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(propTitle);
            listPanel.add(Box.createVerticalStrut(4));
            for (PropertyTile pt : player.getOwnedProperties()) {
                propTiles.add(pt);
                JCheckBox cb = new JCheckBox(
                    pt.getName() + "  (" + pt.getLevelName() + ")  →  ₪"
                    + String.format("%,d", pt.getSellValue()));
                cb.setBackground(BG);
                cb.setForeground(TEXT);
                cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
                cb.setAlignmentX(LEFT_ALIGNMENT);
                cb.addItemListener(e -> updateTotals());
                propBoxes.add(cb);
                listPanel.add(cb);
                listPanel.add(Box.createVerticalStrut(2));
            }
            listPanel.add(Box.createVerticalStrut(6));
        }

        if (!player.getOwnedStations().isEmpty()) {
            JLabel stTitle = new JLabel("Shuttle Stations  (75% of purchase value)");
            stTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
            stTitle.setForeground(MUTED);
            stTitle.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(stTitle);
            listPanel.add(Box.createVerticalStrut(4));
            for (ShuttleStationTile st : player.getOwnedStations()) {
                stations.add(st);
                JCheckBox cb = new JCheckBox(
                    st.getName() + "  (Station)  →  ₪"
                    + String.format("%,d", st.getSellValue()));
                cb.setBackground(BG);
                cb.setForeground(TEXT);
                cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
                cb.setAlignmentX(LEFT_ALIGNMENT);
                cb.addItemListener(e -> updateTotals());
                stationBoxes.add(cb);
                listPanel.add(cb);
                listPanel.add(Box.createVerticalStrut(2));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 100)));
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setPreferredSize(new Dimension(420, 200));
        add(scroll, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setBackground(new Color(10, 10, 30));
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));

        selectedLabel = new JLabel();
        selectedLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectedLabel.setForeground(GOLD);
        selectedLabel.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(selectedLabel);

        projectedLabel = new JLabel();
        projectedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        projectedLabel.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(projectedLabel);
        footer.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(new Color(10, 10, 30));
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        sellBtn = new JButton("SELL SELECTED");
        sellBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        sellBtn.setBackground(new Color(35, 120, 55));
        sellBtn.setForeground(Color.WHITE);
        sellBtn.setFocusPainted(false);
        sellBtn.setBorderPainted(false);
        sellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sellBtn.setEnabled(false);
        sellBtn.addActionListener(e -> doSell());
        btnRow.add(sellBtn);

        JButton giveUpBtn = new JButton("DECLARE BANKRUPTCY");
        giveUpBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        giveUpBtn.setBackground(new Color(120, 30, 30));
        giveUpBtn.setForeground(Color.WHITE);
        giveUpBtn.setFocusPainted(false);
        giveUpBtn.setBorderPainted(false);
        giveUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        giveUpBtn.addActionListener(e -> dispose());
        btnRow.add(giveUpBtn);

        footer.add(btnRow);
        add(footer, BorderLayout.SOUTH);

        updateTotals();
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void updateTotals() {
        int selected  = getSelectedSellValue();
        int projected = player.getMoney() + selected;
        int shortage  = amountNeeded - player.getMoney();

        headerLabel.setText("You owe: ₪" + String.format("%,d", amountNeeded)
            + "   |   You have: ₪" + String.format("%,d", player.getMoney())
            + "   |   Shortage: ₪" + String.format("%,d", Math.max(0, shortage)));

        selectedLabel.setText("Selected sell value: ₪" + String.format("%,d", selected));

        boolean enough = projected >= amountNeeded;
        projectedLabel.setText("After selling: ₪" + String.format("%,d", projected)
            + (enough ? "  ✓ Enough to pay!" : "  ✗ Still short ₪" + String.format("%,d", amountNeeded - projected)));
        projectedLabel.setForeground(enough ? GREEN : RED_COL);

        sellBtn.setEnabled(selected > 0);
    }

    private int getSelectedSellValue() {
        int total = 0;
        for (int i = 0; i < propBoxes.size(); i++)
            if (propBoxes.get(i).isSelected()) total += propTiles.get(i).getSellValue();
        for (int i = 0; i < stationBoxes.size(); i++)
            if (stationBoxes.get(i).isSelected()) total += stations.get(i).getSellValue();
        return total;
    }

    private void doSell() {
        for (int i = propBoxes.size() - 1; i >= 0; i--) {
            if (propBoxes.get(i).isSelected()) {
                player.sellProperty(propTiles.get(i));
                propBoxes.remove(i);
                propTiles.remove(i);
            }
        }
        for (int i = stationBoxes.size() - 1; i >= 0; i--) {
            if (stationBoxes.get(i).isSelected()) {
                player.sellStation(stations.get(i));
                stationBoxes.remove(i);
                stations.remove(i);
            }
        }
        dispose();
    }
}
