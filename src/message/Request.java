package message;

import aor.GameState;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    public final GameState gameState;

    protected Request() {
        gameState = null;
    }

    protected Request(GameState gameState) {
        this.gameState = gameState;
    }
}
