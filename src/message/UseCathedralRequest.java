package message;

import aor.GameState;

public class UseCathedralRequest extends Request<BooleanResponse> {

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
}
