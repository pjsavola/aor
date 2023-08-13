package aor;

import message.CommodityResponse;
import message.SelectCommodityRequest;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class DoubleCommodityCard extends Card {
    private final Commodity commodity1;
    private final Commodity commodity2;

    public DoubleCommodityCard(Commodity commodity1, Commodity commodity2) {
        super(Commodity.capitalized(commodity1) + " / " + Commodity.capitalized(commodity2), false);
        this.commodity1 = commodity1;
        this.commodity2 = commodity2;
    }

    @Override
    public void play(Server game, Player player) {
        super.play(game, player);
        final Commodity commodity = new FutureOrDefault<>(player, new SelectCommodityRequest(game.getGameState(), List.of(commodity1, commodity2))).get().getCommodity();
        System.err.println("Played as " + commodity);
        game.commodityPlayed(commodity);
    }

    @Override
    public void render(Graphics g, int x, int y, int width, int height) {
        super.render(g, x, y, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int dx = 5;
        int dy = 60;
        String values = "";
        for (int i = 1; i <= 3; ++i) {
            values += (commodity1.getValue() * i * i);
            values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
        dy += g.getFontMetrics().getHeight();
        values = "";
        for (int i = 4; i <= 6; ++i) {
            values += (commodity1.getValue() * i * i);
            values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
        dy += g.getFontMetrics().getHeight();
        values = "";
        for (int i = 7; i <= 9; ++i) {
            values += (commodity1.getValue() * i * i);
            if (i != 9) values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
    }
}
