package message;

import aor.*;

import java.util.*;
import java.util.stream.Collectors;

public class ExpansionRequest extends Request<ExpansionResponse> {
    public final int playerIndex;
    public final int tokens;
    private final transient Set<Node> reachableUnlimited;
    private final transient Set<Node> reachableLimited;
    private final transient Map<Node, Integer> usedShipping;
    private final transient int shipCapacity;

    public ExpansionRequest(GameState gameState, int playerIndex, int tokens, Set<Node> reachableUnlimited, Set<Node> reachableLimited, Map<Node, Integer> usedShipping, int shipCapacity) {
        super("Expand", gameState);
        this.playerIndex = playerIndex;
        this.tokens = tokens;
        this.reachableUnlimited = reachableUnlimited;
        this.reachableLimited = reachableLimited;
        this.usedShipping = usedShipping;
        this.shipCapacity = shipCapacity;
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        if (response.getTokensUsed().values().stream().mapToInt(Integer::intValue).sum() + response.getTokensDisbanded() <= tokens) {
            return false;
        }

        final PlayerState playerState = gameState.turnOrder.get(playerIndex);
        return response.getTokensUsed().entrySet().stream().allMatch(e -> {
            final String name = e.getKey();
            final int tokenss = e.getValue();
            Node node = reachableLimited.stream().filter(n -> n.getName().equals(name)).findAny().orElse(null);
            if (node != null) {
                final int used = usedShipping.getOrDefault(node, 0);
                if (used + tokens > shipCapacity) {
                    return false;
                }
            } else {
                node = reachableUnlimited.stream().filter(n -> n.getName().equals(name)).findAny().orElse(null);
            }
            if (node != null) {
                int requiredTokens = node.getSize();
                for (int i = 0; i < gameState.turnOrder.size(); ++i) {
                    if (i == playerIndex) continue;

                    final PlayerState p = gameState.turnOrder.get(i);
                    int defenderTokens = p.areas.getOrDefault(node.getName(), 0) + p.newAreas.getOrDefault(node.getName(), 0);
                    if (defenderTokens == 0) continue;

                    if (p.capital == node.getCapital()) {
                        defenderTokens *= 2;
                    }
                    requiredTokens += defenderTokens;
                    final int mod = Game.getAttackModifier(playerState.weapons, p.weapons);
                    requiredTokens -= mod;
                }
                return tokens == Math.min(node.getSize(), requiredTokens);
            }
            return false;
        });
    }

    @Override
    public ExpansionResponse getDefaultResponse() {
        return new ExpansionResponse(Collections.emptyMap(), tokens);
    }
}
