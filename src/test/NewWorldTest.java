package test;

import aor.Advance;
import message.*;

import java.util.ArrayList;
import java.util.List;

public class NewWorldTest {

    private static void testFailure(TestClient client, int tokens, String area) {
        final ExpansionResponse response = new ExpansionResponse(tokens);
        response.addTokens(area, tokens);
        client.addReponse(response, false);

    }

    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(3);
        final TestServer server = Test.initializeTestWithAdvances(clients, 3, 500, 2, Advance.heavens);

        ExpansionResponse expansionResponseBarcelona = new ExpansionResponse(2);
        expansionResponseBarcelona.addTokens("Sicily", 2);
        clients.get(2).addReponse(expansionResponseBarcelona, true);

        ExpansionResponse expansionResponseGenoa = new ExpansionResponse(2);
        expansionResponseGenoa.addTokens("Tunis", 2);
        clients.get(1).addReponse(expansionResponseGenoa, true);

        ExpansionResponse expansionResponseVenice = new ExpansionResponse(2);
        expansionResponseVenice.addTokens("Belgrade", 2);
        clients.get(0).addReponse(expansionResponseVenice, true);

        for (int i = 0; i < clients.size(); ++i) {
            final TestClient client = clients.get(i);
            client.addReponse(new IntegerResponse(4), true); // Bid for expansion
            if (i == 2) client.addReponse(new CommodityResponse(null), true); // Do not remove shortage/surplus
            client.addReponse(new IntegerResponse(-1), true); // Do not play anything
            client.addReponse(new BooleanResponse(true), true); // Advance ships
            PurchaseAdvancesResponse response = new PurchaseAdvancesResponse();
            if (i != 1) response.addAdvance(Advance.overlandEast);
            if (i >= 1) response.addAdvance(Advance.seaworthyVessels);
            if (i == 2) response.addAdvance(Advance.oceanNavigation);

            client.addReponse(response, true);
            client.addReponse(new BooleanResponse(true), true); // Stabilization

            final ExpansionResponse expansionResponse = new ExpansionResponse(4);
            if (i == 2) expansionResponse.addTokens("East Indies", 4);
            client.addReponse(expansionResponse, true);

            client.addReponse(new IntegerResponse(10), true); // Bid for expansion
            if (i == 2) client.addReponse(new CommodityResponse(null), true); // Do not remove shortage/surplus
            client.addReponse(new IntegerResponse(-1), true); // Do not play anything
            client.addReponse(new BooleanResponse(true), true); // Advance ships

            response = new PurchaseAdvancesResponse();
            if (i == 2) {
                response.addAdvance(Advance.newWorld);
                client.addReponse(response, false);
                response = new PurchaseAdvancesResponse();
                response.addAdvance(Advance.urbanAscendancy);
                response.addAdvance(Advance.newWorld);
            }
            client.addReponse(response, true);
            client.addReponse(new BooleanResponse(true), true); // Stabilization
        }

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
