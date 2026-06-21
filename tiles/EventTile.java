package tiles;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import cards.EventCard;
import players.Player;
import players.HumanPlayer;

public class EventTile implements Tile {
    private String name;
    private ArrayList<EventCard> deck;

    public EventTile(String name, ArrayList<EventCard> deck) {
        this.name = name;
        this.deck = deck;
    }

    @Override
    public String getName() { return name; }

    public EventCard drawCard() {
        Collections.shuffle(deck);
        return deck.get(0);
    }

    @Override
    public void landOn(Player player) {
        Collections.shuffle(deck);
        EventCard card = deck.get(0);
        if (player instanceof HumanPlayer) {
            JOptionPane.showMessageDialog(null,
                card.getDescription() + "\n\nClick OK to collect.",
                name + " — Event Card", JOptionPane.INFORMATION_MESSAGE);
        }
        card.apply(player);
    }
}
