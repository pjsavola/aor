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
        final TestServer server = Test.initializeTestWithAdvances(clients, 3, 60, 20, Advance.heavens);

        ExpansionResponse expansionResponseBarcelona = new ExpansionResponse(20);
        expansionResponseBarcelona.addTokens("Sicily", 2);
        expansionResponseBarcelona.addTokens("Bari", 1);
        expansionResponseBarcelona.addTokens("Fez", 2);
        expansionResponseBarcelona.addTokens("Algers", 1);
        expansionResponseBarcelona.addTokens("Tunis", 1);
        clients.get(2).addReponse(expansionResponseBarcelona, true);

        ExpansionResponse expansionResponseGenoa = new ExpansionResponse(20);
        expansionResponseGenoa.addTokens("Granada", 2);
        expansionResponseGenoa.addTokens("Tunis", 2);
        expansionResponseGenoa.addTokens("Valencia", 2);
        clients.get(1).addReponse(expansionResponseGenoa, true);

        testFailure(clients.get(0), 1, "Durazzo"); // Durazzo is not reachable
        testFailure(clients.get(0), 1, "Bari"); // Bari is already occupied by Barcelona
        testFailure(clients.get(0), 1, "Adriatic Sea"); // Can't put tokens in water

        ExpansionResponse expansionResponseVenice = new ExpansionResponse(20);
        expansionResponseVenice.addTokens("Belgrade", 1);
        expansionResponseVenice.addTokens("Bari", 2);
        clients.get(0).addReponse(expansionResponseVenice, true);

        for (int i = 0; i < clients.size(); ++i) {
            final TestClient client = clients.get(i);
            client.addReponse(new IntegerResponse(10), true); // Bid 10 for expansion
            if (i == 2) client.addReponse(new CommodityResponse(null), true); // Do not remove shortage/surplus
            client.addReponse(new IntegerResponse(-1), true); // Do not play anything
            if (i != 0) client.addReponse(new BooleanResponse(true), true); // Advance ships, Venice doesn't have cash
            client.addReponse(new PurchaseAdvancesResponse(), true);
            if (i == 0) client.addReponse(new BooleanResponse(true), true); // Venice stabilizes using cash
        }

        testFailure(clients.get(2), 1, "Acre"); // Requires usage of two seas
        testFailure(clients.get(2), 2, "St. Malo"); // Moving to locked Paris area
        testFailure(clients.get(2), 2, "Edinburg"); // Moving to locked London area
        testFailure(clients.get(2), 2, "Angora"); // Moving to locked Black Sea area

        expansionResponseBarcelona = new ExpansionResponse(10);
        expansionResponseBarcelona.addTokens("Bordeaux", 3);
        expansionResponseBarcelona.addTokens("Cyprus", 3);
        expansionResponseBarcelona.addTokens("Suez", 3);
        clients.get(2).addReponse(expansionResponseBarcelona, true);

        /*expansionResponseGenoa = new ExpansionResponse(20);
        expansionResponseGenoa.addTokens("Granada", 2);
        expansionResponseGenoa.addTokens("Tunis", 2);
        expansionResponseGenoa.addTokens("Valencia", 2);
        clients.get(1).addReponse(expansionResponseGenoa, true);

        expansionResponseVenice = new ExpansionResponse(20);
        expansionResponseVenice.addTokens("Belgrade", 1);
        expansionResponseVenice.addTokens("Bari", 2);
        clients.get(0).addReponse(expansionResponseVenice, true);*/

        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(1);
        server.predefinedRandomNumbers.add(0);
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 1
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 2
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 3
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 4
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 5
        server.predefinedRandomNumbers.add(0); // Shortage/surplus roll 6
        new Thread(server).start();
    }
}
