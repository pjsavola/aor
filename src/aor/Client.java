package aor;

import message.Notification;
import message.Request;
import message.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements Runnable {
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final Socket socket;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                final Object message = ois.readObject();
                if (message instanceof Request) {
                    oos.writeObject(getResponse((Request<?>) message));
                } else if (message instanceof Notification) {
                    //return handleNotification((Notification) message);
                }
            } catch (EOFException e) {
                // This is ok, no objects to read
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error when reading object input from server");
                break;
            }
        }
        if (!socket.isClosed()) {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            ois.close();
        } catch (IOException e) {
            System.err.println("Error when closing server connection");
        }
        try {
            oos.close();
        } catch (IOException e) {
            System.err.println("Error when closing server connection");
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error when closing server connection");
        }
    }

    private <T extends Request<U>, U extends Response> U getResponse(T request) {
        return request.getDefaultResponse();
    }
}
