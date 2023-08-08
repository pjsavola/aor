package message;

import aor.GameState;

import java.io.Serial;
import java.io.Serializable;

public abstract class Request<U extends Response> implements Serializable {
    public final String info;
    public final GameState gameState;

    protected Request(String info) {
        this(info, null);
    }

    protected Request(String info, GameState gameState) {
        this.info = info;
        this.gameState = gameState;
    }

    public abstract boolean validateResponse(U response);

    public abstract U getDefaultResponse();
}
