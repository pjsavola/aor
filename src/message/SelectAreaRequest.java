package message;

import aor.GameState;

import java.io.Serial;

public class SelectAreaRequest extends Request<IntegerResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
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
