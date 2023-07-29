import java.util.List;

public abstract class Card {

    private final String name;
    final boolean singleUse;
    private Card[] invalidates;

    protected Card(String name, boolean singleUse) {
        this.name = name;
        this.singleUse = singleUse;
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
