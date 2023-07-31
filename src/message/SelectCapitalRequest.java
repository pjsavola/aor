package message;

import aor.GameState;
import aor.Node;

import java.util.Set;

public class SelectCapitalRequest extends Request<CapitalResponse> {
    public final Set<Node.CityState> options;

    public SelectCapitalRequest(String info, GameState gameState, Set<Node.CityState> options) {
        super(info, gameState);
        this.options = options;
    }

    @Override
    public boolean validateResponse(CapitalResponse response) {
        return options.contains(response.getCapital());
    }

    @Override
    public CapitalResponse getDefaultResponse() {
        return new CapitalResponse(options.iterator().next());
    }
}
