package message;

import aor.Commodity;
import aor.GameState;

import java.util.Set;

public class AdjustShortageSurplusRequest extends Request<CommodityReponse> {
    private final Set<Commodity> options;

    public AdjustShortageSurplusRequest(String info, GameState gameState, Set<Commodity> options) {
        super(info, gameState);
        this.options = options;
    }

    @Override
    public boolean validateResponse(CommodityReponse response) {
        return response.getCommodity() == null || (options.contains(response.getCommodity()) && Math.abs(response.getAdjustment()) == 1);
    }

    @Override
    public CommodityReponse getDefaultResponse() {
        return new CommodityReponse(null);
    }
}
