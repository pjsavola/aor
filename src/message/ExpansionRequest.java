package message;

import aor.*;

import java.util.*;
import java.util.stream.Collectors;

public class ExpansionRequest extends Request<ExpansionResponse> {
    public final int playerIndex;
    public final int tokens;
    private final transient Set<Node> reachableUnlimited;
    private final transient Map<Node, Integer> capacityMap;

    public ExpansionRequest(GameState gameState, int playerIndex, int tokens, Set<Node> reachableUnlimited, Map<Node, Integer> capacityMap) {
        super("Expand", gameState);
        this.playerIndex = playerIndex;
        this.tokens = tokens;
        this.reachableUnlimited = reachableUnlimited;
        this.capacityMap = capacityMap;
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        if (response.getTokensUsed().values().stream().mapToInt(Integer::intValue).sum() + response.getTokensDisbanded() <= tokens) {
            return false;
        }

        final PlayerState playerState = gameState.turnOrder.get(playerIndex);
        return response.getTokensUsed().entrySet().stream().allMatch(e -> {
            final String name = e.getKey();
            final int tokenCount = e.getValue();
            final Node node = capacityMap.entrySet().stream()
                    .filter(entry -> entry.getKey().getName().equals(name) && entry.getValue() >= tokenCount)
                    .map(Map.Entry::getKey).findAny()
                    .orElse(reachableUnlimited.stream().filter(n -> n.getName().equals(name)).findAny().orElse(null));
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
                return tokenCount == Math.min(node.getSize(), requiredTokens);
            }
            return false;
        });
    }

    @Override
    public ExpansionResponse getDefaultResponse() {
        return new ExpansionResponse(Collections.emptyMap(), tokens);
    }
}
