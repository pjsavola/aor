package message;

import aor.Client;
import aor.GameState;

import java.io.Serial;

public class SelectHolyIndulgencePaymentRequest extends Request<BooleanResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public SelectHolyIndulgencePaymentRequest(GameState gameState) {
        super("Not enough tokens to pay off Holy Indulgence. Pay remaining tokens with cash?", gameState);
    }

    @Override
    public boolean validateResponse(BooleanResponse response) {
        return true;
    }

    @Override
    public BooleanResponse getDefaultResponse() {
        return new BooleanResponse(true);
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
