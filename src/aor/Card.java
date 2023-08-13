package aor;

import message.CardNotification;
import message.CardPlayNotification;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Card {
    public static List<Card> allCards = new ArrayList<>();
    private final int index;
    private final String name;
    final boolean singleUse;
    private int epoch;
    private Card[] invalidates;

    protected Card(String name, boolean singleUse) {
        this.name = name;
        this.singleUse = singleUse;
        index = allCards.size();
        allCards.add(this);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean canPlay(Server game) {
        return !game.unplayableCards.contains(this);
    }

    public Card invalidates(int epoch, Card... cards) {
        this.epoch = epoch;
        invalidates = cards;
        return this;
    }

    public void play(Server game, Player player) {
        game.log(player + " played " + name);
        player.notifyOthers(new CardPlayNotification(this, game.players.indexOf(player)));
        game.playedCards.add(this);
        if (game.getEpoch() >= epoch) {
            if (invalidates != null) {
                System.err.println(Arrays.stream(invalidates).map(c -> c.name).collect(Collectors.joining(", ")) + " becomes unplayable misery burden.");
                game.unplayableCards.addAll(List.of(invalidates));
            }
        }
    }

    public void render(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        int w;
        int fontSize = 16;
        do {
            g.setFont(new Font("Arial", Font.PLAIN, fontSize));
            w = g.getFontMetrics().stringWidth(name);
            --fontSize;
        } while (width - 10 < w);
        g.drawString(name, x + (width - w) / 2, y + 20);
    }
}
