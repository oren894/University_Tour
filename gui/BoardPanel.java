package gui;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import players.Player;

// Draws the 36-tile board, player tokens, and ownership/building overlays.
public class BoardPanel extends JPanel {

    // Board geometry
    static final int BOARD_SIZE = 680;
    static final int CORNER     = 84;
    static final int SIDE       = 64;  // (680 - 84*2) / 8 = 64

    // Player token colours (shared with SidebarPanel)
    static final Color[] PLAYER_COLORS = {
        new Color(220, 55,  55),
        new Color(55,  100, 220),
        new Color(50,  185, 80),
        new Color(225, 185, 0)
    };

    private ArrayList<Player> players = new ArrayList<>();

    public BoardPanel() {
        setPreferredSize(new Dimension(BOARD_SIZE + 20, BOARD_SIZE + 20));
        setBackground(new Color(15, 15, 30));
    }

    public void setPlayers(ArrayList<Player> p) { players = new ArrayList<>(p); }

    private int[] buildingLevels;
    { java.util.Arrays.fill(buildingLevels = new int[36], -1); }
    public void setBuildingLevels(int[] levels) { buildingLevels = levels; }

    private int[] tileOwnerIndex;
    { java.util.Arrays.fill(tileOwnerIndex = new int[36], -1); }
    public void setTileOwnerIndex(int[] owners) { tileOwnerIndex = owners; }

    private int[] tileRent;
    { java.util.Arrays.fill(tileRent = new int[36], -1); }
    public void setTileRent(int[] rent) { tileRent = rent; }

    private int wcTileIndex = -1;
    public void setWCTileIndex(int idx) { wcTileIndex = idx; }

    private Color ownerColor(int tileIdx) {
        int pi = tileOwnerIndex[tileIdx];
        return pi >= 0 ? PLAYER_COLORS[pi % PLAYER_COLORS.length] : new Color(50, 185, 65);
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Scale board to fill the panel, keeping aspect ratio, centered
        int pW = getWidth(), pH = getHeight();
        double scale = Math.min(pW, pH) / (double)(BOARD_SIZE + 20);
        int tx = (int)((pW - (BOARD_SIZE + 20) * scale) / 2);
        int ty = (int)((pH - (BOARD_SIZE + 20) * scale) / 2);
        g2.translate(tx, ty);
        g2.scale(scale, scale);
        g2.translate(10, 10);

        drawBoard(g2);
        if (!players.isEmpty()) drawPlayerTokens(g2);

        g2.dispose();
    }

    // ── Board ─────────────────────────────────────────────────────────────────

    private void drawBoard(Graphics2D g2) {
        // Outer background gradient
        g2.setPaint(new GradientPaint(0, 0, new Color(195, 228, 195),
                                      BOARD_SIZE, BOARD_SIZE, new Color(160, 205, 160)));
        g2.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        // Centre felt
        g2.setColor(new Color(140, 190, 140));
        g2.fillRect(CORNER, CORNER, BOARD_SIZE - 2 * CORNER, BOARD_SIZE - 2 * CORNER);

        drawCenterText(g2);

        for (int i = 0; i < 36; i++) drawTile(g2, i);

        // Board outer border
        g2.setColor(new Color(25, 55, 25));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        // Inner divider line
        g2.setColor(new Color(40, 90, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(CORNER, CORNER, BOARD_SIZE - 2 * CORNER, BOARD_SIZE - 2 * CORNER);
    }

    private void drawCenterText(Graphics2D g2) {
        int cx = BOARD_SIZE / 2;
        int cy = BOARD_SIZE / 2;
        String[] lines = {"UNIVERSITY", "TOUR"};
        Font font = new Font("Serif", Font.BOLD | Font.ITALIC, 50);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Shadow
        g2.setColor(new Color(0, 0, 0, 35));
        for (int i = 0; i < lines.length; i++) {
            int sw = fm.stringWidth(lines[i]);
            g2.drawString(lines[i], cx - sw / 2 + 3, cy - 18 + i * 58 + 3);
        }
        // Text
        g2.setColor(new Color(35, 85, 35));
        for (int i = 0; i < lines.length; i++) {
            int sw = fm.stringWidth(lines[i]);
            g2.drawString(lines[i], cx - sw / 2, cy - 18 + i * 58);
        }
        // Sub-line
        g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g2.setColor(new Color(60, 110, 60));
        String sub = "A Monopoly-Style Board Game";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 82);
    }

    // ── Tile drawing ──────────────────────────────────────────────────────────

    private void drawTile(Graphics2D g2, int index) {
        Rectangle r = getTileBounds(index);
        if (r == null) return;

        // Background – special tiles get tinted backgrounds
        g2.setColor(tileBackground(index));
        g2.fillRect(r.x, r.y, r.width, r.height);

        // Property colour stripe
        Color stripe = propertyColor(index);
        if (stripe != null) drawStripe(g2, r, index, stripe);

        // Grid border
        g2.setColor(new Color(90, 120, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(r.x, r.y, r.width, r.height);

        // Text label
        drawTileLabel(g2, r, index);

        // Buildings (houses / hotel)
        drawBuildings(g2, r, index);

        // ── World Championships glow ─────────────────────────────────────────
        if (index == wcTileIndex) {
            // gold outer glow (multiple strokes, decreasing alpha)
            for (int pass = 3; pass >= 1; pass--) {
                g2.setColor(new Color(255, 200, 0, 55 * pass));
                g2.setStroke(new BasicStroke(pass * 2.0f));
                g2.drawRect(r.x + pass, r.y + pass, r.width - pass * 2, r.height - pass * 2);
            }
            // solid gold border
            g2.setColor(new Color(255, 200, 0, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
            // ★ badge in corner
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("★WC", r.x + 2, r.y + r.height - 3);
        }
    }

    private static final int[] STATION_TILES = {8, 10, 19, 28};

    private void drawBuildings(Graphics2D g2, Rectangle r, int index) {
        // ── Shuttle station icon ──────────────────────────────────────────────
        for (int si : STATION_TILES) {
            if (index == si) {
                if (tileOwnerIndex[index] < 0) return; // unowned, no icon
                Color pc = ownerColor(index);
                int cx = r.x + r.width / 2, cy = r.y + r.height / 2;
                int rad = 9;
                // outer ring in player colour
                g2.setColor(pc);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx - rad, cy - rad, rad * 2, rad * 2);
                // filled inner dot
                g2.setColor(pc.darker());
                g2.fillOval(cx - 4, cy - 4, 8, 8);
                // "S" label
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("S", cx - fm.stringWidth("S") / 2, cy + fm.getAscent() / 2 - 1);
                return;
            }
        }

        int lvl = buildingLevels[index];
        if (lvl < 0) return;

        Color pc = ownerColor(index);

        if (lvl == 0) {
            // Flag pole in player colour
            int px = r.x + r.width / 2 - 2;
            int poleTop = r.y + r.height / 2 - 8;
            int poleBot = r.y + r.height / 2 + 5;
            g2.setColor(pc.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(px, poleTop, px, poleBot);
            int[] xs = {px, px + 9, px};
            int[] ys = {poleTop, poleTop + 4, poleTop + 8};
            g2.setColor(pc);
            g2.fillPolygon(xs, ys, 3);
            g2.setColor(pc.darker().darker());
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawPolygon(xs, ys, 3);
            return;
        }

        if (lvl == 4) {
            // Hotel — player-colored rounded rect
            int hw = r.width * 2 / 3, hh = 11;
            int hx = r.x + (r.width  - hw) / 2;
            int hy = r.y + (r.height - hh) / 2;
            g2.setColor(pc);
            g2.fillRoundRect(hx, hy, hw, hh, 4, 4);
            g2.setColor(pc.darker().darker());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(hx, hy, hw, hh, 4, 4);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 7));
            FontMetrics fm = g2.getFontMetrics();
            String lbl = "HOTEL";
            g2.drawString(lbl, hx + (hw - fm.stringWidth(lbl)) / 2, hy + fm.getAscent());
        } else {
            // Houses — player-colored squares
            int hs = 8, gap = 2;
            int totalW = lvl * hs + (lvl - 1) * gap;
            int hx = r.x + (r.width  - totalW) / 2;
            int hy = r.y + (r.height - hs)      / 2;
            for (int i = 0; i < lvl; i++) {
                int x = hx + i * (hs + gap);
                g2.setColor(pc);
                g2.fillRect(x, hy, hs, hs);
                g2.setColor(pc.darker().darker());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(x, hy, hs, hs);
            }
        }
    }

    private void drawStripe(Graphics2D g2, Rectangle r, int idx, Color c) {
        final int sw = 17;
        int sx, sy, sw2, sh;
        if      (idx >= 1  && idx <= 8)  { sx=r.x+1; sy=r.y+1;              sw2=r.width-2;  sh=sw; }
        else if (idx >= 10 && idx <= 17) { sx=r.x+1; sy=r.y+1;              sw2=sw;          sh=r.height-2; }
        else if (idx >= 19 && idx <= 26) { sx=r.x+1; sy=r.y+r.height-sw-1; sw2=r.width-2;  sh=sw; }
        else if (idx >= 28 && idx <= 35) { sx=r.x+r.width-sw-1; sy=r.y+1;  sw2=sw;          sh=r.height-2; }
        else return;

        g2.setColor(c);
        g2.fillRect(sx, sy, sw2, sh);

        // ── Rent label on the stripe ─────────────────────────────────────────
        int rent = tileRent[idx];
        if (rent < 0) return;
        String label = "₪" + (rent >= 1000 ? (rent / 1000) + "k" : rent);

        g2.setFont(new Font("SansSerif", Font.BOLD, 7));
        FontMetrics fm = g2.getFontMetrics();

        // pick a contrasting text colour
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        g2.setColor(hsb[2] > 0.65f ? new Color(30, 30, 30) : Color.WHITE);

        boolean vertical = (idx >= 10 && idx <= 17) || (idx >= 28 && idx <= 35);
        if (vertical) {
            java.awt.geom.AffineTransform old = g2.getTransform();
            int cx = sx + sw2 / 2;
            int cy = sy + sh / 2;
            g2.translate(cx, cy);
            g2.rotate(-Math.PI / 2);
            g2.drawString(label, -fm.stringWidth(label) / 2, fm.getAscent() / 2 - 1);
            g2.setTransform(old);
        } else {
            g2.drawString(label, sx + (sw2 - fm.stringWidth(label)) / 2, sy + fm.getAscent() + 1);
        }
    }

    private void drawTileLabel(Graphics2D g2, Rectangle r, int idx) {
        String[] lines = tileLines(idx);
        if (lines == null) return;

        boolean isCorner = (idx == 0 || idx == 9 || idx == 18 || idx == 27);
        Font f = isCorner ? new Font("SansSerif", Font.BOLD, 9) : new Font("SansSerif", Font.PLAIN, 7);
        g2.setFont(f);
        g2.setColor(new Color(25, 25, 25));
        FontMetrics fm = g2.getFontMetrics();

        int sw = 17; // stripe width
        int tX, tY, tW, tH;

        if (isCorner) {
            tX = r.x + 2; tY = r.y + 2; tW = r.width - 4; tH = r.height - 4;
        } else if (idx >= 1  && idx <= 8)  { tX=r.x+1;    tY=r.y+sw+2;  tW=r.width-2;    tH=r.height-sw-4; }
        else if   (idx >= 10 && idx <= 17) { tX=r.x+sw+2; tY=r.y+2;     tW=r.width-sw-4; tH=r.height-4;    }
        else if   (idx >= 19 && idx <= 26) { tX=r.x+1;    tY=r.y+2;     tW=r.width-2;    tH=r.height-sw-4; }
        else                               { tX=r.x+2;    tY=r.y+2;     tW=r.width-sw-4; tH=r.height-4;    }

        int lineH = fm.getHeight();
        int startY = tY + Math.max(0, (tH - lines.length * lineH) / 2) + fm.getAscent();

        for (String line : lines) {
            int lw = fm.stringWidth(line);
            g2.drawString(line, tX + Math.max(0, (tW - lw) / 2), startY);
            startY += lineH;
        }
    }

    // ── Player tokens ─────────────────────────────────────────────────────────

    private void drawPlayerTokens(Graphics2D g2) {
        int[] slotUsed = new int[36];
        int[][] offX = {{-9, 9, -9,  9}, {-9, 9, -9,  9}};
        int[][] offY = {{-9,-9,  9,  9}, {-9,-9,  9,  9}};

        for (int pi = 0; pi < players.size(); pi++) {
            Player p = players.get(pi);
            if (p.isBankrupt()) continue;

            int pos = p.getPosition();
            Rectangle r = getTileBounds(pos);
            if (r == null) continue;

            int slot = slotUsed[pos]++;
            int cx = r.x + r.width  / 2 + (slot > 0 ? offX[0][slot % 4] : 0);
            int cy = r.y + r.height / 2 + (slot > 0 ? offY[0][slot % 4] : 0);
            int ts = 20;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - ts/2 + 2, cy - ts/2 + 2, ts, ts);

            // Token body
            Color tc = PLAYER_COLORS[pi % PLAYER_COLORS.length];
            g2.setColor(tc);
            g2.fillOval(cx - ts/2, cy - ts/2, ts, ts);

            // Border
            g2.setColor(tc.darker().darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - ts/2, cy - ts/2, ts, ts);

            // Initial
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String init = String.valueOf(p.getName().charAt(0)).toUpperCase();
            g2.drawString(init, cx - fm.stringWidth(init)/2, cy + fm.getAscent()/2 - 1);
        }
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    static Rectangle getTileBounds(int idx) {
        if (idx == 0)  return new Rectangle(0,                 BOARD_SIZE - CORNER, CORNER, CORNER);
        if (idx == 9)  return new Rectangle(BOARD_SIZE-CORNER, BOARD_SIZE - CORNER, CORNER, CORNER);
        if (idx == 18) return new Rectangle(BOARD_SIZE-CORNER, 0,                   CORNER, CORNER);
        if (idx == 27) return new Rectangle(0,                 0,                   CORNER, CORNER);

        if (idx >= 1  && idx <= 8)
            return new Rectangle(CORNER + (idx-1)*SIDE, BOARD_SIZE-CORNER, SIDE, CORNER);
        if (idx >= 10 && idx <= 17)
            return new Rectangle(BOARD_SIZE-CORNER, BOARD_SIZE-CORNER-(idx-9)*SIDE, CORNER, SIDE);
        if (idx >= 19 && idx <= 26)
            return new Rectangle(BOARD_SIZE-CORNER-(idx-18)*SIDE, 0, SIDE, CORNER);
        if (idx >= 28 && idx <= 35)
            return new Rectangle(0, CORNER+(idx-28)*SIDE, CORNER, SIDE);
        return null;
    }

    // ── Tile data ─────────────────────────────────────────────────────────────

    private Color tileBackground(int i) {
        switch (i) {
            case  0: return new Color(255, 238, 130); // Start – gold
            case  9: return new Color(195, 215, 255); // Jail – blue
            case 18: return new Color(200, 235, 255); // Airport – cyan
            case 27: return new Color(230, 200, 255); // World Championships – purple
            case  4: case 12: case 21: case 30: return new Color(255, 238, 200); // Chance
            case  8: case 10: case 19: case 28: return new Color(210, 210, 210); // Shuttle Station
            default: return new Color(253, 251, 235);
        }
    }

    static Color propertyColor(int i) {
        switch (i) {
            case 1: case 2: case 3:      return new Color(150, 90,  50);  // Brown
            case 5: case 6: case 7:      return new Color(90,  185, 220); // Light Blue
            case 11: case 13: case 14:   return new Color(210, 80,  155); // Pink
            case 15: case 16: case 17:   return new Color(245, 140, 30);  // Orange
            case 20: case 22: case 23:   return new Color(225, 40,  40);  // Red
            case 24: case 25: case 26:   return new Color(240, 215, 0);   // Yellow
            case 29: case 31: case 32:   return new Color(45,  160, 50);  // Green
            case 33: case 34: case 35:   return new Color(30,  85,  185); // Dark Blue
            case 8: case 10: case 19: case 28: return new Color(80, 80, 80); // Shuttle
            default: return null;
        }
    }

    private String[] tileLines(int i) {
        switch (i) {
            case  0: return new String[]{"START"};
            case  1: return new String[]{"Malas"};
            case  2: return new String[]{"Optics"};
            case  3: return new String[]{"Linear", "Algebra"};
            case  4: return new String[]{"CHANCE"};
            case  5: return new String[]{"Calculus", "1"};
            case  6: return new String[]{"Calculus", "2"};
            case  7: return new String[]{"Memphis", "3"};
            case  8: return new String[]{"507", "Station"};
            case  9: return new String[]{"JAIL"};
            case 10: return new String[]{"Engineering", "Station"};
            case 11: return new String[]{"Data", "Struct.", "1"};
            case 12: return new String[]{"CHANCE"};
            case 13: return new String[]{"Data", "Struct.", "2"};
            case 14: return new String[]{"Automata", "&", "Computab."};
            case 15: return new String[]{"Computer", "Networks"};
            case 16: return new String[]{"Intro to", "Circuits"};
            case 17: return new String[]{"Circuit", "Design"};
            case 18: return new String[]{"AIRPORT"};
            case 19: return new String[]{"Highway 4", "Station"};
            case 20: return new String[]{"Electricity", "& Magnet."};
            case 21: return new String[]{"CHANCE"};
            case 22: return new String[]{"Mechanics"};
            case 23: return new String[]{"Probability"};
            case 24: return new String[]{"Radar", "Systems"};
            case 25: return new String[]{"Thermo-", "dynamics"};
            case 26: return new String[]{"Algebraic", "Structures"};
            case 27: return new String[]{"WORLD", "CHAMP."};
            case 28: return new String[]{"Tommy", "Station"};
            case 29: return new String[]{"Complex", "Analysis"};
            case 30: return new String[]{"CHANCE"};
            case 31: return new String[]{"Calculus", "3"};
            case 32: return new String[]{"Assembler", "(x86)"};
            case 33: return new String[]{"Operating", "Systems"};
            case 34: return new String[]{"OOP"};
            case 35: return new String[]{"Software", "Eng."};
            default: return null;
        }
    }
}
