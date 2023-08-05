package aor;

import message.Notification;
import message.Request;
import message.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby implements Runnable {
    private final int playerCount;
    private final ServerSocket serverSocket;
    private final List<ClientConnection> clients;

    public Lobby(int playerCount, int port) throws IOException {
        this.playerCount = playerCount;
        this.serverSocket = new ServerSocket(port);
        clients = new ArrayList<>(playerCount);
    }

    @Override
    public void run() {
        boolean done = false;
        while (!done) {
            System.err.println("Waiting for clients");
            try {
                final Socket socket = serverSocket.accept();
                if (clients.size() < playerCount) {
                    clients.add(new ClientConnection(socket));
                    if (clients.size() == playerCount) {
                        done = true;
                        System.err.println("Ready to start");
                    }
                } else {
                    System.err.println("Game is full");
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<ClientConnection> getConnections() {
        return clients;
    }
}
