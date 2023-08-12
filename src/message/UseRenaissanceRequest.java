package message;

import aor.Client;
import aor.GameState;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UseRenaissanceRequest extends Request<IntegerResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<Integer> options;

    public UseRenaissanceRequest(GameState gameState, Set<Integer> options) {
        super("Use Renaissance?", gameState);
        this.options = new ArrayList<>(options);
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() == 0 || options.contains(response.getInt());
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(0);
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
