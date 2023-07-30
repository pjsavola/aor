package message;

import aor.GameState;

public class AdjustShortageSurplusRequest extends Request {
    private final String info;

    public AdjustShortageSurplusRequest(GameState gameState, String info) {
        super(gameState);
        this.info = info;
    }
}
