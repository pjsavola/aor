package test;

import aor.*;
import message.BooleanResponse;
import message.CapitalResponse;
import message.IntegerResponse;
import message.PurchaseAdvancesResponse;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class Test {
    public static Thread initializeTestWithAdvances(List<TestClient> clients, int playerCount, int cash, Advance... advances) {
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < playerCount; ++i) {
                final TestClient client = new TestClient(new JFrame(), new Socket("localhost", 1234));
                client.addReponse(new IntegerResponse(0), true); // Discard 1st card
                client.addReponse(new IntegerResponse(0), true); // Bid 0 for capital
                if (i < playerCount - 1) client.addReponse(new CapitalResponse(Capital.values()[i]), true); // Select capitals
                client.addReponse(new IntegerResponse(20), true); // Bid 20 for expansion
                client.addReponse(new IntegerResponse(-1), true); // Do not play anything from initial hand
                client.addReponse(new BooleanResponse(true), true); // Advance ships
                final PurchaseAdvancesResponse purchaseAdvancesResponse = new PurchaseAdvancesResponse();
                int cost = 0;
                for (Advance advance : advances) {
                    purchaseAdvancesResponse.addAdvance(advance);
                    cost += advance.getCost(Collections.emptySet());
                }
                client.addReponse(purchaseAdvancesResponse, true);
                if (cash - cost < 3) client.addReponse(new BooleanResponse(false), true); // Stabilize using misery
                clients.add(client);
                final Thread clientThread = new Thread(client);
                clientThread.start();
            }
            final TestServer server = new TestServer(lobby.getConnections()) {
                @Override
                protected List<Card> getDeck(int epoch) {
                    return super.getDeck(epoch);
                }

                @Override
                protected int getInitialCash() {
                    return cash;
                }
            };
            server.predefinedShuffles = Integer.MAX_VALUE;
            return new Thread(server);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }
}
