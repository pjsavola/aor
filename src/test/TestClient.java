package test;

import aor.Client;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class TestClient extends Client {
    public TestClient(JFrame frame, Socket socket) throws IOException {
        super(frame, socket, false);
    }
}
