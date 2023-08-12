package aor;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class AoR {
    public static void main(String[] args) {

        final int playerCount = 4;
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < 4; ++i) {
                final Thread clientThread = new Thread(new Client(new JFrame(), new Socket("localhost", 1234), i != 0));
                clientThread.start();
            }
            final Thread serverThread = new Thread(new Server(lobby.getConnections()));
            serverThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}