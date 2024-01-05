package test;

import aor.*;
import message.*;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeavensTest {

    private static void testFailure(TestClient client, int tokens, String area) {
        final ExpansionResponse response = new ExpansionResponse(tokens);
        response.addTokens(area, tokens);
        client.addReponse(response, false);

    }

    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(3);
        final TestServer server = Test.initializeTestWithAdvances(clients, 3, 60, Advance.heavens);

        final ExpansionResponse expansionResponseBarcelona = new ExpansionResponse(20);
        expansionResponseBarcelona.addTokens("Sicily", 2);
        expansionResponseBarcelona.addTokens("Bari", 1);
        expansionResponseBarcelona.addTokens("Fez", 2);
        expansionResponseBarcelona.addTokens("Algers", 1);
        expansionResponseBarcelona.addTokens("Tunis", 1);
        clients.get(2).addReponse(expansionResponseBarcelona, true);

        final ExpansionResponse expansionResponseGenoa = new ExpansionResponse(20);
        expansionResponseGenoa.addTokens("Granada", 2);
        expansionResponseGenoa.addTokens("Tunis", 2);
        expansionResponseGenoa.addTokens("Valencia", 2);
        clients.get(1).addReponse(expansionResponseGenoa, true);

        testFailure(clients.get(0), 1, "Durazzo"); // Durazzo is not reachable
        testFailure(clients.get(0), 1, "Bari"); // Bari is already occupied by Barcelona
        testFailure(clients.get(0), 1, "Adriatic Sea"); // Can't put tokens in water

        final ExpansionResponse expansionResponseVenice = new ExpansionResponse(20);
        expansionResponseVenice.addTokens("Belgrade", 1);
        expansionResponseVenice.addTokens("Bari", 2);
        clients.get(0).addReponse(expansionResponseVenice, true);

        for (int i = 0; i < clients.size(); ++i) {
            final TestClient client = clients.get(i);
            client.addReponse(new IntegerResponse(20), true); // Bid 20 for expansion
            if (i == 2) client.addReponse(new CommodityResponse(null), true); // Do not remove shortage/surplus
            client.addReponse(new IntegerResponse(-1), true); // Do not play anything from initial hand
            client.addReponse(new BooleanResponse(true), true); // Advance ships
        }

        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(1);
        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 1
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 2
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 3
        new Thread(server).start();
    }
}
