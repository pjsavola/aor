public class Card {

    private final String name;
    final boolean singleUse;

    private Card[] invalidates;

    public Card(String name, boolean singleUse) {
        this.name = name;
        this.singleUse = singleUse;
    }

    public Card invalidates(Card... cards) {
        invalidates = cards;
        return this;
    }

    public void play(Game game, Player player) {
        game.playedCards.add(this);
    }
}
