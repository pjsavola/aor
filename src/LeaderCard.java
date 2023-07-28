import java.util.concurrent.locks.Condition;

public class LeaderCard extends Card {

    final int amount;
    final Card condition;
    final int extra;
    final Advance[] advances;

    private Player owner;
    private int usesRemaining;

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

    public boolean canUse(Player player) {
        return owner == player || (usesRemaining > 0 && player.getAdvances().contains(Advance.patronage));
    }

    public void use(Player player) {
        if (owner != player) {
            --usesRemaining;
        }
    }

    @Override
    public void play(Game game, Player player) {
        owner = player;
        usesRemaining = game.patronageQueue.size();
        game.patronageQueue.add(this);
    }
}
