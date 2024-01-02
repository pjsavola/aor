package test;

import aor.*;
import message.ExpansionRequest;
import message.ExpansionResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CathedralTest {

    private static GameState createGameState(int playerCount) {
        final GameState gameState = new GameState();
        gameState.round = 1;
        gameState.players = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; ++i) {
            gameState.players.add(new PlayerState());
            gameState.players.get(i).capital = Capital.values()[i];
            gameState.players.get(i).advances = new int[0];
        }
        return gameState;
    }

    private static void addAdvance(GameState gameState, int playerIndex, Advance advance) {
        final PlayerState playerState = gameState.players.get(playerIndex);
        final int advanceIndex = Advance.allAdvances.indexOf(advance);
        final int[] newAdvances = new int[playerState.advances.length + 1];
        for (int i = 0; i < playerState.advances.length; ++i) {
            newAdvances[i] = playerState.advances[i];
            if (advanceIndex == newAdvances[i]) {
                return;
            }
        }
        newAdvances[playerState.advances.length] = advanceIndex;
        playerState.advances = newAdvances;

        if (advance == Advance.cathedral) {
            for (int i = 0; i < gameState.players.size(); ++i) {
                if (gameState.players.get(i).cathedralUsed == null) {
                    gameState.players.get(i).cathedralUsed = new int[gameState.players.size()];
                }
            }
        }
    }

    private static void addTokens(GameState gameState, int playerIndex, Node node, int count) {
        int total = 0;
        for (int i = 0; i < gameState.players.size(); ++i) {
            final PlayerState playerState = gameState.players.get(i);
            final int idx = playerState.areas.indexOf(node.getName());
            final int tokens = idx == -1 ? 0 : playerState.tokens.get(idx);
            total += tokens;
            if (playerIndex == i) {
                if (total > 0 && count + total >= node.getSize()) {
                    throw new RuntimeException("Illegal amount of tokens");
                }
                total += count;
                if (idx == -1) {
                    playerState.areas.add(node.getName());
                    playerState.tokens.add(count);
                } else {
                    playerState.tokens.set(idx, tokens + count);
                }
            }
        }
        if (total > node.getSize()) {
            throw new RuntimeException("Illegal amount of tokens");
        }
    }

    public static void main(String[] args) {
        final String target = "Aleppo"; // Size 4
        new Board(null, "map.jpg").load(new File("map.dat")); // Init node map
        final Node node = Node.nodeMap.get(target);

        GameState gameState = createGameState(2);
        addTokens(gameState, 1, node, 4);
        addAdvance(gameState, 0, Advance.cathedral);
        ExpansionRequest request = new ExpansionRequest(gameState, 0, 20, Set.of(node), Collections.emptyMap(), 3);

        ExpansionResponse response = new ExpansionResponse(8);
        response.addTokens(target, 8);
        if (!request.validateResponse(response)) throw new RuntimeException("Attack without Cathedral");

        response = new ExpansionResponse(8);
        response.addTokens(target, 8);
        response.setCathedralUsed(node.getName());
        if (!request.validateResponse(response)) throw new RuntimeException("Attack with Cathedral");

        response = new ExpansionResponse(7);
        response.addTokens(target, 7);
        response.setCathedralUsed(node.getName());
        if (request.validateResponse(response)) throw new RuntimeException("Attack with Cathedral using insufficient tokens");

        // Player 2 gets Cathedral
        addAdvance(gameState, 1, Advance.cathedral);

        response = new ExpansionResponse(8);
        response.addTokens(target, 8);
        response.setCathedralUsed(node.getName());
        if (request.validateResponse(response)) throw new RuntimeException("Attack with Cathedral against Cathedral");

        response = new ExpansionResponse(8);
        response.addTokens(target, 8);
        if (!request.validateResponse(response)) throw new RuntimeException("Attack without using Cathedral against Cathedral");

        gameState = createGameState(4);
        addTokens(gameState, 1, node, 1);
        addTokens(gameState, 2, node, 1);
        addTokens(gameState, 3, node, 1);
        addAdvance(gameState, 0, Advance.cathedral);
        request = new ExpansionRequest(gameState, 0, 20, Set.of(node), Collections.emptyMap(), 3);

        response = new ExpansionResponse(7);
        response.addTokens(target, 7);
        response.setCathedralUsed(node.getName());
        if (!request.validateResponse(response)) throw new RuntimeException("Attack with Cathedral vs. multiple players");

        // Player 3 gets Cathedral
        addAdvance(gameState, 2, Advance.cathedral);

        response = new ExpansionResponse(7);
        response.addTokens(target, 7);
        response.setCathedralUsed(node.getName());
        if (request.validateResponse(response)) throw new RuntimeException("Attack with Cathedral vs. multiple players and one of them has Cathedral");

        response = new ExpansionResponse(7);
        response.addTokens(target, 7);
        if (!request.validateResponse(response)) throw new RuntimeException("Attack without using Cathedral vs. multiple players and one of them has Cathedral");
    }
}
