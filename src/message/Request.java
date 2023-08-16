package message;

import aor.Client;
import aor.GameState;
import aor.Node;

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

    public String getInfo() {
        return info;
    }

    public abstract boolean validateResponse(U response);

    public abstract U getDefaultResponse();

    public abstract void handleRequest(Client client);

    public boolean clicked(Response response, Node node) {
        return false;
    }

    public boolean highlight(Response response, Node node) {
        return false;
    }

    public Response reset() {
        return null;
    }
}
