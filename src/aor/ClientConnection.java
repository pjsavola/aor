package aor;

import message.Request;
import message.Notification;
import message.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {
    private static int counter;
    private static final int heartBeatMs = 200;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final Socket socket;
    private final int index;

    public ClientConnection(Socket socket) {
        this.socket = socket;
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error when initializing Client " + socket.getInetAddress());
            disconnect();
        }
        index = counter++;
    }

    public boolean isConnected() {
        return ois != null && oos != null;
    }

    public void disconnect() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            socket.close();
        } catch (IOException e) {
            System.err.println("Error when terminating Client " + socket.getInetAddress());
        }
        ois = null;
        oos = null;
    }

    public synchronized <N extends Notification> void notify(N notification) {
        if (oos != null) {
            try {
                System.err.println("Server " + index + " sends notification " + notification);
                oos.writeObject(notification);
            } catch (IOException e) {
                System.err.println("Error when notifying Client " + socket.getInetAddress());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends Request<U>, U extends Response> U request(T request) {
        if (isConnected()) {
            try {
                System.err.println("Server " + index + " sends request " + request);
                oos.writeObject(request);
                Object response;
                do {
                    response = getResponse();
                } while (!(response instanceof Response));
                System.err.println("Server " + index + " received response " + response.getClass().getSimpleName());
                return (U) response;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Lost connection to Client " + socket.getInetAddress());
                disconnect();
            }
        }
        return null;
    }

    private Object getResponse() throws IOException, ClassNotFoundException {
        try {
            return ois.readObject();
        } catch (EOFException e) {
            // this might be ok if there's no response yet
            // test whether connection still exists by writing empty notification
            try {
                Thread.sleep(heartBeatMs);
            } catch (InterruptedException exception) {
                e.printStackTrace();
            }
            oos.writeObject(new Notification());
        }
        return null;
    }
}
