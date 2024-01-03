package test;

import aor.Card;
import aor.Client;
import aor.Lobby;
import aor.Server;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        final int playerCount = 4;
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < 4; ++i) {
                final Thread clientThread = new Thread(new TestClient(new JFrame(), new Socket("localhost", 1234)));
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
