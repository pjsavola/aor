package message;

import aor.GameState;

public class SelectCivilWarLossesRequest extends Request<BooleanResponse> {

    public SelectCivilWarLossesRequest(GameState gameState) {
        super("Lose tokens to Civil War?", gameState);
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
