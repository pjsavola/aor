package message;

import aor.Capital;
import aor.Client;
import aor.GameState;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectCapitalRequest extends Request<CapitalResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<Capital> options;

    public SelectCapitalRequest(String info, GameState gameState, Set<Capital> options) {
        super(info, gameState);
        this.options = new ArrayList<>(options);
    }

    @Override
    public boolean validateResponse(CapitalResponse response) {
        return options.contains(response.getCapital());
    }

    @Override
    public CapitalResponse getDefaultResponse() {
        return new CapitalResponse(options.iterator().next());
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
