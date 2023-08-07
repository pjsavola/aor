package message;

import aor.GameState;

import java.io.Serial;

public class UpgradeShipsRequest extends Request<BooleanResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public UpgradeShipsRequest(GameState gameState) {
        super("Upgrade Ships?", gameState);
    }

    @Override
    public boolean validateResponse(BooleanResponse response) {
        return response != null;
    }

    @Override
    public BooleanResponse getDefaultResponse() {
        return new BooleanResponse(false);
    }
}
