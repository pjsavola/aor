package message;

import aor.GameState;

public class StabilizationRequest extends Request<BooleanResponse> {

    public StabilizationRequest(GameState gameState) {
        super("Stabilize using cash?", gameState);
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
