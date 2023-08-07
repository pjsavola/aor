package message;

import aor.Commodity;
import aor.GameState;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdjustShortageSurplusRequest extends Request<CommodityReponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<Commodity> options;

    public AdjustShortageSurplusRequest(String info, GameState gameState, Set<Commodity> options) {
        super(info, gameState);
        this.options = new ArrayList<>(options);
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
