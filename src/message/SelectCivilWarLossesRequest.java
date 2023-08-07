package message;

import aor.GameState;

import java.io.Serial;

public class SelectCivilWarLossesRequest extends Request<BooleanResponse> {

    @Serial
    private static final long serialVersionUID = 1L;
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
