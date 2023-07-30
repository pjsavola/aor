package aor;

public class LeaderCard extends Card {

    final int amount;
    final Card condition;
    final int boostedAmount;
    final Advance[] advances;

    private Player owner;
    private int usesRemaining;

    public LeaderCard(String name, int amount, Advance ... advances) {
        this(name, amount, null, 0, advances);
    }

    public LeaderCard(String name, int amount, Card condition, int boostedAmount, Advance ... advances) {
        super(name, true);
        this.amount = amount;
        this.condition = condition;
        this.boostedAmount = boostedAmount;
        this.advances = advances;
    }

    public int getAmount(Game game) {
        return condition != null && game.playedCards.contains(condition) ? boostedAmount : amount;
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
        super.play(game, player);
        player.gainRebate(this);
        owner = player;
        usesRemaining = game.patronageQueue.size();
        game.patronageQueue.add(this);
    }
}
