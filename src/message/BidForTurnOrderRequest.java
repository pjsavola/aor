package message;

import aor.GameState;

public class BidForTurnOrderRequest extends Request {
    public BidForTurnOrderRequest(GameState gameState) {
        super(gameState);
    }
}
