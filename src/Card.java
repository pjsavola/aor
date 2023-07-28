public class Card {

    private final String name;

    private Card[] invalidates;

    public Card(String name) {
        this.name = name;
    }

    public Card invalidates(Card... cards) {
        invalidates = cards;
        return this;
    }

    public void play(Game game, Player player) {

    }
}
