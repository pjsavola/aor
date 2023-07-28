import java.util.concurrent.locks.Condition;

public class LeaderCard extends Card {

    private final int amount;
    private final Card condition;
    private final int extra;
    private final Advance[] advances;

    public LeaderCard(String name, int amount, Advance ... advances) {
        this(name, amount, null, 0, advances);
    }

    public LeaderCard(String name, int amount, Card condition, int extra, Advance ... advances) {
        super(name);
        this.amount = amount;
        this.condition = condition;
        this.extra = extra;
        this.advances = advances;
    }

    @Override
    public void play(Game game, Player player) {

    }
}
