package message;

import aor.*;

import javax.swing.*;
import java.io.Serial;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExpansionRequest extends Request<ExpansionResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final int playerIndex;
    public final int tokens;
    public final int cardCost;
    private final List<String> reachableUnlimited;
    private final List<String> capacityMapKeys;
    private final List<Integer> capacityMapValues;

    public ExpansionRequest(GameState gameState, int playerIndex, int tokens, Set<Node> reachableUnlimited, Map<Node, Integer> capacityMap, int cardCost) {
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
        this.cardCost = cardCost;
    }

    public int getCapacity(String area) {
        final int unlimitedIdx = reachableUnlimited.indexOf(area);
        if (unlimitedIdx != -1) return Integer.MAX_VALUE;
        final int capacityIdx = capacityMapKeys.indexOf(area);
        return capacityIdx == -1 ? 0 : capacityMapValues.get(capacityIdx);
    }

    public int getUsedCapacity(Node node) {
        int totalTokenCount = 0;
        for (int i = 0; i < gameState.players.size(); ++i) {
            final PlayerState p = gameState.players.get(i);
            final int idx = p.areas.indexOf(node.getName());
            if (idx != -1) totalTokenCount += p.tokens.get(idx);
            final int newIdx = p.newAreas.indexOf(node.getName());
            if (newIdx != -1) totalTokenCount += p.newTokens.get(newIdx);
        }
        return totalTokenCount;
    }

    public int getOpponentTokens(Node node, int myPlayerIndex) {
        int totalTokenCount = 0;
        for (int i = 0; i < gameState.players.size(); ++i) {
            if (i == myPlayerIndex) continue;

            final PlayerState p = gameState.players.get(i);
            final int idx = p.areas.indexOf(node.getName());
            if (idx != -1) totalTokenCount += p.tokens.get(idx);
            final int newIdx = p.newAreas.indexOf(node.getName());
            if (newIdx != -1) totalTokenCount += p.newTokens.get(newIdx);
        }
        return totalTokenCount;
    }

    public int getRequiredTokensToAttack(Node node) {
        final PlayerState playerState = gameState.players.get(playerIndex);
        final boolean cosmopolitan = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.cosmopolitan);
        return getRequiredTokensToAttack(node, cosmopolitan);
    }

    private int getRequiredTokensToAttack(Node node, boolean cosmopolitan) {
        final PlayerState playerState = gameState.players.get(playerIndex);
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

        final PlayerState p = gameState.players.get(playerIndex);
        int attackerTokens = 0;
        final int idx = p.areas.indexOf(node.getName());
        if (idx != -1) attackerTokens += p.tokens.get(idx);
        final int newIdx = p.newAreas.indexOf(node.getName());
        if (newIdx != -1) attackerTokens += p.newTokens.get(newIdx);
        requiredTokens -= attackerTokens;
        if (p.capital == node.getCapital()) {
            requiredTokens = (requiredTokens + 1) / 2;
        }

        return Math.max(node.getSize() - attackerTokens, requiredTokens);
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        if (gameState.deckSize == 0 && response.isCardPurchased()) {
            return false;
        }
        final int cardCosts = response.isCardPurchased() ? cardCost : 0;
        if (response.getTokensUsed() + response.getTokensDisbanded() + cardCosts > tokens) {
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
                final int totalTokenCount = getUsedCapacity(node);
                if (totalTokenCount + tokenCount < node.getSize()) {
                    return true;
                }

                return tokenCount == getRequiredTokensToAttack(node, cosmopolitan);
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

    @Override
    public boolean clicked(Response pendingResponse, Node node, JFrame frame) {
        final ExpansionResponse response = (ExpansionResponse) pendingResponse;
        final int usedTokens = response.getTokensUsed();
        final int alreadyPlacedTokens = response.getTokens(node.getName());
        final int freeCapacity = node.getSize() - getUsedCapacity(node) - alreadyPlacedTokens;
        final int neededTokens;
        if (freeCapacity > 1) {
            neededTokens = 1;
        } else {
            neededTokens = getRequiredTokensToAttack(node) - alreadyPlacedTokens;
        }
        if (neededTokens + alreadyPlacedTokens <= getCapacity(node.getName())) {
            if (usedTokens + neededTokens <= tokens) {
                if (getOpponentTokens(node, playerIndex) == 0) {
                    response.addTokens(node.getName(), neededTokens);
                    return true;
                } else {
                    final int result = JOptionPane.showConfirmDialog(frame, "Attack " + node.getName() + " with " + neededTokens + " more tokens?", "Attack?", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        response.addTokens(node.getName(), neededTokens);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean highlight(Response response, Node node) {
        return getCapacity(node.getName()) > 0;
    }

    @Override
    public Response reset() {
        return new ExpansionResponse(tokens);
    }
}
