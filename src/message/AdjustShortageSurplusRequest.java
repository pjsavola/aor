package message;

import aor.Client;
import aor.Commodity;
import aor.GameState;

import java.io.Serial;
import java.util.*;

public class AdjustShortageSurplusRequest extends Request<CommodityResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final Map<Commodity, Integer> options;

    public AdjustShortageSurplusRequest(String info, GameState gameState, Map<Commodity, Integer> options) {
        super(info, gameState);
        this.options = new LinkedHashMap<>(options);
    }

    @Override
    public boolean validateResponse(CommodityResponse response) {
        final Commodity commodity = response.getCommodity();
        if (commodity == null) return true;
        if (Math.abs(response.getAdjustment()) != 1) return false;
        if (!options.containsKey(commodity)) return false;

        final Integer allowedAdjustment = options.get(commodity);
        return allowedAdjustment == null || allowedAdjustment == response.getAdjustment();
    }

    @Override
    public CommodityResponse getDefaultResponse() {
        return new CommodityResponse(null);
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
