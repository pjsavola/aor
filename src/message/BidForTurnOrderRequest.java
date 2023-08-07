package message;

import aor.GameState;

import java.io.Serial;

public class BidForTurnOrderRequest extends Request<IntegerResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final int cash;

    public BidForTurnOrderRequest(GameState gameState, int cash) {
        super("Bid for expansion tokens and turn order", gameState);
        this.cash = cash;
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return Math.abs(response.getInt()) <= cash;
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(0);
    }
}
