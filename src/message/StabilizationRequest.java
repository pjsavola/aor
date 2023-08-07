package message;

import aor.GameState;

import java.io.Serial;

public class StabilizationRequest extends Request<BooleanResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
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
