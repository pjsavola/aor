public class DoubleCommodityCard extends Card {
    private final Commodity commodity1;
    private final Commodity commodity2;

    public DoubleCommodityCard(Commodity commodity1, Commodity commodity2) {
        super(Commodity.capitalized(commodity1) + " / " + Commodity.capitalized(commodity2));
        this.commodity1 = commodity1;
        this.commodity2 = commodity2;
    }

    @Override
    public void play(Game game, Player player) {

    }
}
