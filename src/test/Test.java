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
    public static TestServer initializeTestWithAdvances(List<TestClient> clients, int playerCount, int cash, int tokenBid, boolean advanceShips, Advance... advances) {
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < playerCount; ++i) {
                final TestClient client = new TestClient(new JFrame(), new Socket("localhost", 1234));
                client.addResponse(new IntegerResponse(0), true); // Discard 1st card
                client.addResponse(new IntegerResponse(0), true); // Bid 0 for capital
                if (i < playerCount - 1) client.addResponse(new CapitalResponse(Capital.values()[i]), true); // Select capitals
                client.addResponse(new IntegerResponse(tokenBid), true); // Bid for expansion
                client.addResponse(new IntegerResponse(-1), true); // Do not play anything from initial hand
                client.addResponse(new BooleanResponse(advanceShips), true); // Advance ships
                final PurchaseAdvancesResponse purchaseAdvancesResponse = new PurchaseAdvancesResponse();
                int cost = tokenBid + (advanceShips ? 10 : 0);
                for (Advance advance : advances) {
                    purchaseAdvancesResponse.addAdvance(advance);
                    cost += advance.getCost(Collections.emptySet());
                }
                client.addResponse(purchaseAdvancesResponse, true);
                if (cash - cost >= 3) client.addResponse(new BooleanResponse(false), true); // Stabilize using misery
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
            return server;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }
}
