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

    private static void addAdvance(GameState gameState, int playerIndex, Advance advance) {
        final PlayerState playerState = gameState.players.get(playerIndex);
        final int advanceIndex = Advance.allAdvances.indexOf(advance);
        if (playerState.advances == null) {
            playerState.advances = new int[] { advanceIndex };
        } else {
            final int[] newAdvances = new int[playerState.advances.length + 1];
            for (int i = 0; i < playerState.advances.length; ++i) {
                newAdvances[i] = playerState.advances[i];
                if (advanceIndex == newAdvances[i]) {
                    return;
                }
            }
            newAdvances[playerState.advances.length] = advanceIndex;
            playerState.advances = newAdvances;
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
        final int playerCount = 2;
        final String target = "Aleppo"; // Size 4
        new Board(null, "map.jpg").load(new File("map.dat")); // Init node map
        final Node node = Node.nodeMap.get(target);
        final GameState gameState = new GameState();
        gameState.players = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; ++i) {
            gameState.players.add(new PlayerState());
            gameState.players.get(i).capital = Capital.values()[i];
        }
        addTokens(gameState, 1, node, 4);
        gameState.players.get(0).advances = new int[] { };//Advance.allAdvances.indexOf(Advance.cathedral) };
        gameState.players.get(1).advances = new int[] { };//Advance.allAdvances.indexOf(Advance.cathedral) };

        final ExpansionRequest request = new ExpansionRequest(gameState, 0, 20, Set.of(node), Collections.emptyMap(), 3);
        final ExpansionResponse response = new ExpansionResponse(5);
        response.addTokens(target, 5);
        if (!request.validateResponse(response)) throw new RuntimeException("Fail");
    }
}
