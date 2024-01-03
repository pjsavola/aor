package test;

import aor.*;
import message.CapitalResponse;
import message.IntegerResponse;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        final int playerCount = 3;
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < playerCount; ++i) {
                final TestClient client = new TestClient(new JFrame(), new Socket("localhost", 1234));
                client.predefinedResponses.add(new IntegerResponse(0)); // Discard 1st card
                client.predefinedResponses.add(new IntegerResponse(0)); // Bid 0 for capital
                client.predefinedResponses.add(new CapitalResponse(Capital.values()[i]));
                final Thread clientThread = new Thread(client);
                clientThread.start();
            }
            final TestServer server = new TestServer(lobby.getConnections()) {
                @Override
                protected List<Card> getDeck(int epoch) {
                    return super.getDeck(epoch);
                }
            };
            server.predefinedShuffles = 10;
            final Thread serverThread = new Thread(server);
            serverThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
