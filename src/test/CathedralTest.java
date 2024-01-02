package test;

import aor.*;
import message.ExpansionRequest;
import message.ExpansionResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class CathedralTest {
    public static void main(String[] args) {
        final String target = "Aleppo"; // Size 4
        new Board(null, "map.jpg").load(new File("map.dat")); // Init node map
        final Node node = Node.nodeMap.get(target);
        final GameState gameState = new GameState();
        gameState.players = new ArrayList<>(2);
        for (int i = 0; i < 2; ++i) {
            gameState.players.add(new PlayerState());
            gameState.players.get(i).capital = Capital.values()[i];
        }
        gameState.players.get(1).areas.add(target);
        gameState.players.get(1).tokens.add(1);
        gameState.players.get(0).advances = new int[] { };//Advance.allAdvances.indexOf(Advance.cathedral) };
        gameState.players.get(1).advances = new int[] { };//Advance.allAdvances.indexOf(Advance.cathedral) };

        final ExpansionRequest request = new ExpansionRequest(gameState, 0, 20, Set.of(node), Collections.emptyMap(), 3);
        final ExpansionResponse response = new ExpansionResponse(5);
        response.addTokens(target, 5);
        if (!request.validateResponse(response)) throw new RuntimeException("Fail");
    }
}
