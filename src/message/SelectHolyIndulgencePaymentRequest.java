package message;

import aor.GameState;

public class SelectHolyIndulgencePaymentRequest extends Request<BooleanResponse> {

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
}
