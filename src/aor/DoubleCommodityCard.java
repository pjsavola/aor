package aor;

import message.CommodityResponse;
import message.SelectCommodityRequest;

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
}
