package message;

import aor.*;

import java.io.Serial;
import java.util.*;

public class ExpansionRequest extends Request<ExpansionResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final int playerIndex;
    public int tokens;
    private final List<String> reachableUnlimited;
    private final List<String> capacityMapKeys;
    private final List<Integer> capacityMapValues;

    public ExpansionRequest(GameState gameState, int playerIndex, int tokens, Set<Node> reachableUnlimited, Map<Node, Integer> capacityMap) {
        super("Expand", gameState);
        this.playerIndex = playerIndex;
        this.tokens = tokens;
        this.reachableUnlimited = reachableUnlimited.stream().map(Node::getName).toList();
        capacityMapKeys = new ArrayList<>(capacityMap.size());
        capacityMapValues = new ArrayList<>(capacityMap.size());
        capacityMap.forEach((key, value) -> {
            capacityMapKeys.add(key.getName());
            capacityMapValues.add(value);
        });
    }

    public int getCapacity(String area) {
        final int capacityIdx = capacityMapKeys.indexOf(area);
        return capacityIdx == -1 ? 0 : capacityMapValues.get(capacityIdx);
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        if (response.getTokensUsed() + response.getTokensDisbanded() <= tokens) {
            return false;
        }

        final PlayerState playerState = gameState.players.get(playerIndex);
        final boolean cathedral = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.cosmopolitan);
        if (!cathedral && response.getCathedralused() != null) {
            return false;
        }
        if (cathedral && response.getCathedralused() != null && playerState.cathedralUsed >= gameState.round) {
            return false;
        }
        final boolean cosmopolitan = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.cosmopolitan);
        return response.getEntryStream().allMatch(e -> {
            final String name = e.getKey();
            final int tokenCount = e.getValue();
            final int remainingCapacity = getCapacity(name);
            final boolean allowed = remainingCapacity >= tokenCount || reachableUnlimited.contains(name);
            if (allowed) {
                final Node node = Node.nodeMap.get(name);
                int totalTokenCount = 0;
                for (int i = 0; i < gameState.players.size(); ++i) {
                    final PlayerState p = gameState.players.get(i);
                    final int idx = p.areas.indexOf(node.getName());
                    if (idx != -1) totalTokenCount += p.tokens.get(idx);
                    final int newIdx = p.newAreas.indexOf(node.getName());
                    if (newIdx != -1) totalTokenCount += p.newTokens.get(newIdx);
                }
                if (totalTokenCount + tokenCount < node.getSize()) {
                    return true;
                }

                int requiredTokens = node.getSize();
                for (int i = 0; i < gameState.players.size(); ++i) {
                    if (i == playerIndex) continue;

                    final PlayerState p = gameState.players.get(i);
                    int defenderTokens = 0;
                    final int idx = p.areas.indexOf(node.getName());
                    if (idx != -1) defenderTokens += p.tokens.get(idx);
                    final int newIdx = p.newAreas.indexOf(node.getName());
                    if (newIdx != -1) defenderTokens += p.newTokens.get(newIdx);
                    if (defenderTokens == 0) continue;

                    if (p.capital == node.getCapital()) {
                        defenderTokens *= 2;
                    }
                    requiredTokens += defenderTokens;
                    for (Node support : node.getSupportNodes()) {
                        if (p.areas.contains(support.getName())) ++requiredTokens;
                    }
                    final int mod = Server.getAttackModifier(playerState.weapons, p.weapons);
                    requiredTokens -= mod;
                }
                if (cosmopolitan) {
                    for (Node support : node.getSupportNodes()) {
                        if (playerState.areas.contains(support.getName())) --requiredTokens;
                    }
                }
                return tokenCount == Math.min(node.getSize(), requiredTokens);
            }
            return false;
        });
    }

    @Override
    public ExpansionResponse getDefaultResponse() {
        return new ExpansionResponse(Collections.emptyMap(), tokens, null);
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
