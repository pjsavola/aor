package message;

import aor.*;

import java.util.*;
import java.util.stream.Collectors;

public class ExpansionRequest extends Request<ExpansionResponse> {
    public final int playerIndex;
    public final int tokens;

    public ExpansionRequest(GameState gameState, int playerIndex, int tokens) {
        super("Expand", gameState);
        this.playerIndex = playerIndex;
        this.tokens = tokens;
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        final PlayerState playerState = gameState.turnOrder.get(playerIndex);
        return response.getTokensUsed().values().stream().mapToInt(Integer::intValue).sum() + response.getTokensDisbanded() <= tokens;
    }

    @Override
    public ExpansionResponse getDefaultResponse() {
        return new ExpansionResponse(Collections.emptyMap(), tokens);
    }
}
