package message;

import aor.GameState;

public class SelectAreaRequest extends Request<IntegerResponse> {
    public SelectAreaRequest(GameState gameState) {
        super("Select area for Black Death", gameState);
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() >= 1 && response.getInt() <= 8;
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(8);
    }
}
