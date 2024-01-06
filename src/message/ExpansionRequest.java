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
        final boolean nationalism = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.nationalism) && node.getRegion() == playerState.capital.getRegion();
        return getRequiredTokensToAttack(node, cosmopolitan, nationalism);
    }

    private int getRequiredTokensToAttack(Node node, boolean cosmopolitan, boolean nationalism) {
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
        if (nationalism) {
            --requiredTokens;
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

    private boolean hasTokens(int opponentIndex, Node node) {
        for (int i = 0; i < gameState.players.size(); ++i) {
            if (i != opponentIndex) continue;

            final PlayerState p = gameState.players.get(i);
            int defenderTokens = 0;
            final int idx = p.areas.indexOf(node.getName());
            if (idx != -1) defenderTokens += p.tokens.get(idx);
            final int newIdx = p.newAreas.indexOf(node.getName());
            if (newIdx != -1) defenderTokens += p.newTokens.get(newIdx);
            return defenderTokens > 0;
        }
        return false;
    }

    private static int initColonies(Set<Node> colonies, PlayerState playerState) {
        final boolean oceanNavigation = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.oceanNavigation);
        final boolean newWorld = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.newWorld);
        if (oceanNavigation || newWorld) {
            playerState.areas.stream().map(name -> Node.nodeMap.get(name)).filter(node -> node.isInAsia() || (newWorld && node.isInNewWorld())).forEach(colonies::add);
            playerState.newAreas.stream().map(name -> Node.nodeMap.get(name)).filter(node -> node.isInAsia() || (newWorld && node.isInNewWorld())).forEach(colonies::add);
            return playerState.shipLevel;
        } else {
            return 0;
        }
    }

    @Override
    public boolean validateResponse(ExpansionResponse response) {
        if (response.getTokensDisbanded() < 0) {
            return false;
        }

        if (gameState.deckSize == 0 && response.isCardPurchased()) {
            return false;
        }

        final int cardCosts = response.isCardPurchased() ? cardCost : 0;
        if (response.getTokensUsed() + response.getTokensDisbanded() + cardCosts > tokens) {
            return false;
        }

        final boolean[] cathedrals = new boolean[gameState.players.size()];
        if (response.getCathedralused() != null) {
            for (int i = 0; i < gameState.players.size(); ++i) {
                final PlayerState state = gameState.players.get(i);
                cathedrals[i] = Arrays.stream(state.advances).mapToObj(j -> Advance.allAdvances.get(j)).anyMatch(a -> a == Advance.cathedral);
            }
            if (!cathedrals[playerIndex]) {
                return false;
            }
        }

        final PlayerState playerState = gameState.players.get(playerIndex);
        final boolean cosmopolitan = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.cosmopolitan);
        final boolean nationalism = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.nationalism);
        final Set<Node> colonies = new HashSet<>();
        final int allowedColonies = initColonies(colonies, playerState);
        return response.getEntryStream().allMatch(e -> {
            final String name = e.getKey();
            final int tokenCount = e.getValue();
            final int remainingCapacity = getCapacity(name);
            final boolean allowed = remainingCapacity >= tokenCount || reachableUnlimited.contains(name);
            if (allowed) {
                final Node node = Node.nodeMap.get(name);
                if ((node.isInAsia() || node.isInNewWorld()) && !colonies.contains(node)) {
                    if (allowedColonies <= colonies.size()) {
                        return false;
                    }
                    colonies.add(node);
                }
                if (cathedrals[playerIndex] && response.getCathedralused() != null) {
                    for (int i = 0; i < gameState.players.size(); ++i) {
                        if (i == playerIndex) continue;

                        if (hasTokens(i, node)) {
                            if (cathedrals[i]) {
                                // One of the opponents has Cathedral
                                return false;
                            }
                            if (playerState.cathedralUsed[i] >= gameState.round) {
                                // Cathedral already used vs. this opponent on this round.
                                return false;
                            }
                        }
                    }
                }
                final int totalTokenCount = getUsedCapacity(node);
                if (totalTokenCount + tokenCount < node.getSize()) {
                    return true;
                }

                final boolean useNationalism = nationalism && node.getRegion() == playerState.capital.getRegion();
                return tokenCount == getRequiredTokensToAttack(node, cosmopolitan, useNationalism);
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
    public boolean clicked(Response pendingResponse, Node node, Client client) {
        if (node.getSize() < 1) {
            return false;
        }

        final ExpansionResponse response = (ExpansionResponse) pendingResponse;
        final int usedTokens = response.getTokensUsed();
        if (node.isInAsia() || node.isInNewWorld()) {
            final Set<Node> colonies = new HashSet<>();
            final int allowedColonies = initColonies(colonies, gameState.players.get(playerIndex));
            Node.nodeMap.values().stream().filter(n -> n.isInAsia() || n.isInNewWorld()).filter(n -> response.getTokens(n.getName()) > 0).forEach(colonies::add);
            if (!colonies.contains(node) && colonies.size() >= allowedColonies) {
                return false;
            }
        }

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
                    final boolean[] cathedrals = new boolean[gameState.players.size()];
                    for (int i = 0; i < gameState.players.size(); ++i) {
                        final PlayerState state = gameState.players.get(i);
                        cathedrals[i] = Arrays.stream(state.advances).mapToObj(j -> Advance.allAdvances.get(j)).anyMatch(a -> a == Advance.cathedral);
                    }
                    boolean canUseCathedral = false;
                    if (cathedrals[playerIndex]) {
                        canUseCathedral = true;
                        final PlayerState playerState = gameState.players.get(playerIndex);
                        if (gameState.turnOrder[0] == playerIndex && Server.getTurnOrderThreshold(0, gameState.players.size()) == 1) {
                            final boolean proselytism = Arrays.stream(playerState.advances).mapToObj(j -> Advance.allAdvances.get(j)).anyMatch(a -> a == Advance.proselytism);
                            if (proselytism) {
                                canUseCathedral = false;
                            }
                        }
                        if (canUseCathedral) {
                            for (int i = 0; i < gameState.players.size(); ++i) {
                                if (i == playerIndex) continue;

                                if (hasTokens(i, node)) {
                                    if (cathedrals[i]) {
                                        canUseCathedral = false;
                                        break;
                                    }
                                    if (playerState.cathedralUsed[i] >= gameState.round) {
                                        canUseCathedral = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (canUseCathedral) {
                        final int result = JOptionPane.showConfirmDialog(client.getFrame(), "Use Cathedral to attack " + node.getName() + " with " + neededTokens + " more tokens?", "Attack using Cathedral?", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (result != JOptionPane.CANCEL_OPTION) {
                            response.clearDisbandedTokens();
                            response.addTokens(node.getName(), neededTokens);
                            if (result == JOptionPane.YES_OPTION) {
                                response.setCathedralUsed(node.getName());
                            }
                            client.confirm();
                            return true;
                        }
                    } else {
                        final int result = JOptionPane.showConfirmDialog(client.getFrame(), "Attack " + node.getName() + " with " + neededTokens + " more tokens?", "Attack?", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            response.clearDisbandedTokens();
                            response.addTokens(node.getName(), neededTokens);
                            client.confirm();
                            return true;
                        }
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
