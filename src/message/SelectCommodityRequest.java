package message;

import aor.Advance;
import aor.Client;
import aor.Commodity;
import aor.GameState;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SelectCommodityRequest extends Request<CommodityResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<Commodity> options;

    public SelectCommodityRequest(GameState gameState, Collection<Commodity> options) {
        super("Choose commodity", gameState);
        this.options = new ArrayList<>(options);
    }

    @Override
    public boolean validateResponse(CommodityResponse response) {
        return response.getCommodity() != null && options.contains(response.getCommodity());
    }

    @Override
    public CommodityResponse getDefaultResponse() {
        return new CommodityResponse(options.get(0));
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
