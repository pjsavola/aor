package message;

import aor.GameState;

public class BidForTurnOrderRequest extends Request<IntegerResponse> {
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
