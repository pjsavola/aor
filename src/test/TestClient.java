package test;

import aor.Client;
import message.Request;
import message.Response;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class TestClient extends Client {
    public Deque<Response> predefinedResponses = new ArrayDeque<>();

    public TestClient(JFrame frame, Socket socket) throws IOException {
        super(frame, socket, false);
    }

    @Override
    protected <T extends Request<U>, U extends Response> U getResponse(T request) {
        if (predefinedResponses.isEmpty()) {
            getFrame().requestFocus();
            return super.getResponse(request);
        }
        return (U) predefinedResponses.removeFirst();
    }
}
