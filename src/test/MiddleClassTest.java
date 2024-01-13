package test;

import aor.Advance;
import message.*;

import java.util.ArrayList;
import java.util.List;

public class MiddleClassTest {

    private static void testFailure(TestClient client, int tokens, String area) {
        final ExpansionResponse response = new ExpansionResponse(tokens);
        response.addTokens(area, tokens);
        client.addResponse(response, false);

    }

    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(1);
        final TestServer server = Test.initializeTestWithAdvances(clients, 1, 247, 0, false, Advance.caravan, Advance.improvedAgriculture);

        final TestClient client = clients.get(0);
        client.addResponse(new IntegerResponse(0), true); // bid
        client.addResponse(new CommodityResponse(null), true); // shortage/surplus adjust
        client.addResponse(new IntegerResponse(-1), true); // card play
        client.addResponse(new BooleanResponse(false), true); // ships
        client.addResponse(new PurchaseAdvancesResponse(List.of(Advance.middleClass)), false); // Wind/Watermill missing
        client.addResponse(new PurchaseAdvancesResponse(List.of(Advance.windWaterMill, Advance.middleClass)), true);
        client.addResponse(new BooleanResponse(true), true); // Pay only 3 because stabilization costs are halved
        client.addResponse(new IntegerResponse(26), true); // Extra 10 income.

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
