package test;

import aor.Advance;
import message.*;

import java.util.ArrayList;
import java.util.List;

public class CaravanTest {

    private static void testFailure(TestClient client, int tokens, String area) {
        final ExpansionResponse response = new ExpansionResponse(tokens);
        response.addTokens(area, tokens);
        client.addResponse(response, false);

    }

    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(2);
        final TestServer server = Test.initializeTestWithAdvances(clients, 2, 247, 7, false, Advance.caravan);

        final ExpansionResponse response1 = new ExpansionResponse(7);
        response1.addTokens("Milan", 3);
        response1.addTokens("Florence", 4);
        clients.get(1).addResponse(response1, true);
        clients.get(1).addResponse(new IntegerResponse(8), true);
        clients.get(1).addResponse(new CommodityResponse(null), true);
        clients.get(1).addResponse(new IntegerResponse(-1), true);
        clients.get(1).addResponse(new BooleanResponse(false), true);
        clients.get(1).addResponse(new PurchaseAdvancesResponse(), true);
        clients.get(1).addResponse(new BooleanResponse(false), true);

        final ExpansionResponse response2 = new ExpansionResponse(7);
        response2.addTokens("Lyons", 4);
        response2.addTokens("Belgrade", 2);
        clients.get(0).addResponse(response2, true);
        clients.get(0).addResponse(new IntegerResponse(36), true);
        clients.get(0).addResponse(new IntegerResponse(-1), true);
        clients.get(0).addResponse(new BooleanResponse(false), true);
        clients.get(0).addResponse(new PurchaseAdvancesResponse(), true);
        clients.get(0).addResponse(new BooleanResponse(false), true);

        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(1);
        server.predefinedRandomNumbers.add(0);
        for (int i = 0; i < 10; ++i) {
            server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 1
            server.predefinedRandomNumbers.add(5); // Shortage/surplus roll 2
            server.predefinedRandomNumbers.add(3); // Shortage/surplus roll 3
        }
        new Thread(server).start();
    }
}
