package message;

import aor.GameState;

public class UseUrbanAscendancyRequest extends Request<BooleanResponse> {

    public UseUrbanAscendancyRequest(GameState gameState) {
        super("Use Urban Ascendancy?", gameState);
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
