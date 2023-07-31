package message;

import aor.GameState;

import java.util.Set;

public class UseRenaissanceRequest extends Request<IntegerResponse> {
    public final Set<Integer> options;

    public UseRenaissanceRequest(GameState gameState, Set<Integer> options) {
        super("Use Renaissance?", gameState);
        this.options = options;
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() == 0 || options.contains(response.getInt());
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(0);
    }
}
