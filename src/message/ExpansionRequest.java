package message;

import aor.*;

import java.util.*;
import java.util.stream.Collectors;

public class ExpansionRequest extends Request<ExpansionResponse> {
    public final int playerIndex;

    public ExpansionRequest(GameState gameState, int playerIndex) {
        super("Purchase Advances", gameState);
        this.playerIndex = playerIndex;
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        final PlayerState playerState = gameState.turnOrder.get(playerIndex);
        return true;
    }

    @Override
    public ExpansionResponse getDefaultResponse() {
        return new ExpansionResponse(0);
    }
}
