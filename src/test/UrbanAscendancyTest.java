package test;

import aor.Advance;
import message.ExpansionResponse;

import java.util.ArrayList;
import java.util.List;

public class UrbanAscendancyTest {

    private static void testFailure(TestClient client, int tokens, String area) {
        final ExpansionResponse response = new ExpansionResponse(tokens);
        response.addTokens(area, tokens);
        client.addReponse(response, false);

    }

    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(1);
        final TestServer server = Test.initializeTestWithAdvances(clients, 1, 44, 30);

        final TestClient client = clients.get(0);
        client.addReponse(new ExpansionResponse(30), true);

        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(1);
        server.predefinedRandomNumbers.add(0);
        for (int i = 0; i < 3; ++i) {
            server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 1
            server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 2
            server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 3
        }
        new Thread(server).start();
    }
}
