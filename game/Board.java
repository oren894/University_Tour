package game;

import java.awt.Color;
import java.util.ArrayList;
import tiles.Tile;
import tiles.StartTile;
import tiles.JailTile;
import tiles.AirportTile;
import tiles.WorldChampionshipsTile;
import tiles.PropertyTile;
import tiles.ShuttleStationTile;
import tiles.EventTile;
import cards.EventCard;

public class Board {
    private ArrayList<Tile> tiles;

    public Board(ArrayList<EventCard> chanceDeck) {
        tiles = new ArrayList<>();
        initBoard(chanceDeck);
    }

    private static final Color BROWN     = new Color(150, 90,  50);
    private static final Color LBLUE     = new Color(90,  185, 220);
    private static final Color PINK      = new Color(210, 80,  155);
    private static final Color ORANGE    = new Color(245, 140, 30);
    private static final Color RED       = new Color(225, 40,  40);
    private static final Color YELLOW    = new Color(240, 215, 0);
    private static final Color GREEN     = new Color(45,  160, 50);
    private static final Color DARK_BLUE = new Color(30,  85,  185);

    private PropertyTile prop(String name, int price, int rent, Color color) {
        PropertyTile p = new PropertyTile(name, price, rent);
        p.setGroupColor(color);
        return p;
    }

    private void initBoard(ArrayList<EventCard> chanceDeck) {
        // ── Bottom side: Start → Jail (positions 0–9) ────────────────────────────
        tiles.add(new StartTile());                                                  //  0  CORNER
        tiles.add(prop("Malas",                     60_000,  10_000, BROWN));       //  1
        tiles.add(prop("Optics",                    60_000,  10_000, BROWN));       //  2
        tiles.add(prop("Linear Algebra",            60_000,  10_000, BROWN));       //  3
        tiles.add(new EventTile("Chance", chanceDeck));                              //  4
        tiles.add(prop("Calculus 1",               100_000,  20_000, LBLUE));       //  5
        tiles.add(prop("Calculus 2",               100_000,  20_000, LBLUE));       //  6
        tiles.add(prop("Memphis 3",                100_000,  20_000, LBLUE));       //  7
        tiles.add(new ShuttleStationTile("507 Station"));                            //  8
        tiles.add(new JailTile());                                                   //  9  CORNER

        // ── Left side: Jail → Airport (positions 10–18) ─────────────────────────
        tiles.add(new ShuttleStationTile("Engineering Station"));                    // 10
        tiles.add(prop("Data Structures 1",        140_000,  30_000, PINK));        // 11
        tiles.add(new EventTile("Chance", chanceDeck));                              // 12
        tiles.add(prop("Data Structures 2",        140_000,  30_000, PINK));        // 13
        tiles.add(prop("Automata & Computability", 140_000,  30_000, PINK));        // 14
        tiles.add(prop("Computer Networks",        180_000,  40_000, ORANGE));      // 15
        tiles.add(prop("Intro to Circuits",        180_000,  40_000, ORANGE));      // 16
        tiles.add(prop("Circuit Design",           180_000,  40_000, ORANGE));      // 17
        tiles.add(new AirportTile(tiles));                                           // 18  CORNER

        // ── Top side: Airport → World Championships (positions 19–27) ────────────
        tiles.add(new ShuttleStationTile("Highway 4 Station"));                      // 19
        tiles.add(prop("Electricity & Magnetism",  220_000,  50_000, RED));         // 20
        tiles.add(new EventTile("Chance", chanceDeck));                              // 21
        tiles.add(prop("Mechanics",                220_000,  50_000, RED));         // 22
        tiles.add(prop("Probability",              220_000,  50_000, RED));         // 23
        tiles.add(prop("Radar Systems",            260_000,  60_000, YELLOW));      // 24
        tiles.add(prop("Thermodynamics",           260_000,  60_000, YELLOW));      // 25
        tiles.add(prop("Algebraic Structures",     260_000,  60_000, YELLOW));      // 26
        tiles.add(new WorldChampionshipsTile());                                     // 27  CORNER

        // ── Right side: World Championships → Start (positions 28–35) ────────────
        tiles.add(new ShuttleStationTile("Tommy Station"));                          // 28
        tiles.add(prop("Complex Analysis",         300_000,  70_000, GREEN));       // 29
        tiles.add(new EventTile("Chance", chanceDeck));                              // 30
        tiles.add(prop("Calculus 3",               300_000,  70_000, GREEN));       // 31
        tiles.add(prop("Assembler (x86)",          300_000,  70_000, GREEN));       // 32
        tiles.add(prop("Operating Systems",        380_000,  90_000, DARK_BLUE));   // 33
        tiles.add(prop("OOP",                      380_000,  90_000, DARK_BLUE));   // 34
        tiles.add(prop("Software Engineering",     380_000,  90_000, DARK_BLUE));   // 35
    }

    public Tile getTile(int position) {
        return tiles.get(position % 36);
    }

    public int getSize() { return tiles.size(); }
}
