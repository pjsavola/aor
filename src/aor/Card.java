package aor;

import java.util.ArrayList;
import java.util.List;

public abstract class Card {
    public static List<Card> allCards = new ArrayList<>();
    private int index;
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

    public boolean canPlay(Server game) {
        return !game.unplayableCards.contains(this);
    }

    public Card invalidates(int epoch, Card... cards) {
        this.epoch = epoch;
        invalidates = cards;
        return this;
    }

    public void play(Server game, Player player) {
        game.playedCards.add(this);
        if (game.getEpoch() >= epoch) {
            game.unplayableCards.addAll(List.of(invalidates));
        }
    }
}
