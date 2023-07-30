package aor;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class Card {
    public static List<Card> allCards = new ArrayList<>();
    private int index;
    private final String name;
    final boolean singleUse;
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

    public boolean canPlay(Game game) {
        return !game.unplayableCards.contains(this);
    }

    public Card invalidates(Card... cards) {
        invalidates = cards;
        return this;
    }

    public void play(Game game, Player player) {
        game.playedCards.add(this);
        game.unplayableCards.addAll(List.of(invalidates));
    }
}
