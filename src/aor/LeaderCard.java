package aor;

import java.awt.*;
import java.util.Set;

public class LeaderCard extends Card {

    final int amount;
    final Card condition;
    final int boostedAmount;
    final Advance[] advances;

    Player owner;
    int usesRemaining;

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

    public int getAmount(Set<Card> playedCards) {
        return condition != null && playedCards.contains(condition) ? boostedAmount : amount;
    }

    public Advance[] getAdvances() {
        return advances;
    }

    public boolean canUse(Player player, Set<Advance> allAdvances) {
        return owner == player || (usesRemaining > 0 && allAdvances.contains(Advance.patronage));
    }

    public void use(Player player) {
        if (owner != player) {
            --usesRemaining;
        }
    }

    @Override
    public void play(Server game, Player player) {
        super.play(game, player);
        player.gainRebate(this);
        owner = player;
        usesRemaining = game.patronageQueue.size();
        game.patronageQueue.add(this);
    }

    @Override
    public void render(Graphics g, int x, int y, int width, int height) {
        super.render(g, x, y, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int dx = 5;
        int dy = 60;
        for (Advance advance : advances) {
            g.drawString(advance.name, x + dx, y + dy);
            dy += g.getFontMetrics().getHeight();
        }
        dy += g.getFontMetrics().getHeight();
        String s = Integer.toString(-amount);
        if (boostedAmount > 0) s += " (" + -boostedAmount + ")";
        g.drawString(s, x + dx, y + dy);
    }
}
