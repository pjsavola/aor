package aor;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AoR {
    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setTitle("Age of Renaissance");

        final Board board = new Board(frame, "map.jpg");
        board.load(new File("map.dat"));
        final int playerCount = 4;
        try {
            final Lobby lobby = new Lobby(playerCount, 1234);
            final Thread lobbyThread = new Thread(lobby);
            lobbyThread.start();
            for (int i = 0; i < 4; ++i) {
                final Thread clientThread = new Thread(new Client(new Socket("localhost", 1234), frame, board, i != 0));
                clientThread.start();
            }
            final Thread serverThread = new Thread(new Server(lobby.getConnections()));
            serverThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    board.esc();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        frame.setContentPane(board);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
    }
}