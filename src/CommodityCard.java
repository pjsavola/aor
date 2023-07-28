public class CommodityCard extends Card {
    private final Commodity commodity;

    public CommodityCard(Commodity commodity) {
        super(Commodity.capitalized(commodity));
        this.commodity = commodity;
    }

    @Override
    public void play(Game game, Player player) {
        game.commodityPlayed(commodity);
    }
}
