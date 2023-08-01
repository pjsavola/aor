package message;

import aor.GameState;

public class UpgradeShipsRequest extends Request<BooleanResponse> {

    public UpgradeShipsRequest(GameState gameState) {
        super("Upgrade Ships?", gameState);
    }

    @Override
    public boolean validateResponse(BooleanResponse response) {
        return true;
    }

    @Override
    public BooleanResponse getDefaultResponse() {
        return new BooleanResponse(false);
    }
}
