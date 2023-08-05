package aor;

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
}
