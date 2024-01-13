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
    private Deque<Response> predefinedResponses = new ArrayDeque<>();
    private Deque<Boolean> predefinedResponseResults = new ArrayDeque<>();

    public TestClient(JFrame frame, Socket socket) throws IOException {
        super(frame, socket, false);
    }

    public void addReponse(Response r, boolean expectation) {
        predefinedResponses.add(r);
        predefinedResponseResults.add(expectation);
    }

    @Override
    protected <T extends Request<U>, U extends Response> U getResponse(T request) {
        while (!predefinedResponses.isEmpty()) {
            final boolean expectation = predefinedResponseResults.removeFirst();
            final U response = (U) predefinedResponses.removeFirst();
            if (expectation && request.validateResponse(response)) {
                return response;
            }
            if (!expectation && !request.validateResponse(response)) {
                continue;
            }
            System.err.println(request);
            request.validateResponse(response);
            throw new RuntimeException("Test failure");
        }
        getFrame().requestFocus();
        return super.getResponse(request);
    }
}
