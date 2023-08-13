package aor;

import java.awt.*;

public class CommodityCard extends Card {
    private final Commodity commodity;

    public CommodityCard(Commodity commodity) {
        super(Commodity.capitalized(commodity), false);
        this.commodity = commodity;
    }

    @Override
    public void play(Server game, Player player) {
        super.play(game, player);
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
            values += (commodity.getValue() * i * i);
            values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
        dy += g.getFontMetrics().getHeight();
        values = "";
        for (int i = 4; i <= 6; ++i) {
            values += (commodity.getValue() * i * i);
            values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
        dy += g.getFontMetrics().getHeight();
        values = "";
        for (int i = 7; i <= 9; ++i) {
            values += (commodity.getValue() * i * i);
            if (i != 9) values += " / ";
        }
        g.drawString(values, x + dx, y + dy);
    }
}
