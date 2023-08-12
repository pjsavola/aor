package message;

import aor.Client;
import aor.GameState;

import java.io.Serial;

public class UseCathedralRequest extends Request<BooleanResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public UseCathedralRequest(GameState gameState) {
        super("Use Cathedral to defend?", gameState);
    }

    @Override
    public boolean validateResponse(BooleanResponse response) {
        return true;
    }

    @Override
    public BooleanResponse getDefaultResponse() {
        return new BooleanResponse(false);
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
