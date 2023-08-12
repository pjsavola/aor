package aor;

import message.*;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Client implements Runnable {
    private static int counter;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final Socket socket;
    private final int index;
    private final JFrame frame;
    private final JPanel board;
    private volatile Response response;

    public Client(Socket socket, JFrame frame, JPanel board) throws IOException {
        this.socket = socket;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
        index = counter++;
        this.frame = frame;
        this.board = board;
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
                e.printStackTrace();
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
        response = null;
        request.handleRequest(this);
        while (response == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return request.getDefaultResponse();
            }
        }
        return (U) response;
    }

    public void handleRequest(SelectCardRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Card> cards = request.getCards();
        if (request.optional) {
            final JButton button = new JButton("Pass");
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = request.getDefaultResponse();
            });
            panel.add(button);
        }
        for (int i = 0; i < cards.size(); ++i) {
            final int index = i;
            final JButton button = new JButton(cards.get(i).getName());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new IntegerResponse(index);
            });
            panel.add(button);
        }
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(SelectCapitalRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Node.CityState> options = request.options;
        for (int i = 0; i < options.size(); ++i) {
            final int index = i;
            final JButton button = new JButton();
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new IntegerResponse(index);
            });
            panel.add(button);
        }
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(SelectCommodityRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Commodity> options = request.options;
        for (int i = 0; i < options.size(); ++i) {
            final int index = i;
            final JButton button = new JButton();
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new IntegerResponse(index);
            });
            panel.add(button);
        }
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(SelectCategoryRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Advance.Category> categories = request.options;
        {
            final JButton button = new JButton("Pass");
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = request.getDefaultResponse();
            });
            panel.add(button);
        }
        for (int i = 0; i < categories.size(); ++i) {
            final int index = i;
            final JButton button = new JButton(categories.get(i).name());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new IntegerResponse(index);
            });
            panel.add(button);
        }
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(BidForCapitalRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        panel.add(new JLabel("Bid:"));
        final JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(100, 20));
        panel.add(field);
        final JButton button = new JButton("Confirm");
        panel.add(button);
        button.addActionListener(l -> {
            final String text = field.getText().trim();
            try {
                final int bid = Integer.parseInt(text);
                final IntegerResponse response = new IntegerResponse(bid);
                if (request.validateResponse(response)) {
                    this.response = response;
                    dialog.setVisible(false);
                    dialog.dispose();
                } else {
                    // Show error message
                }
            } catch (NumberFormatException e) {
                // Show error message
            }
        });
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(BidForTurnOrderRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        panel.add(new JLabel("Bid:"));
        final JTextField field = new JTextField();
        panel.add(field);
        final JButton button = new JButton("Confirm");
        panel.add(button);
        button.addActionListener(l -> {
            final String text = field.getText().trim();
            try {
                final int bid = Integer.parseInt(text);
                final IntegerResponse response = new IntegerResponse(bid);
                if (request.validateResponse(response)) {
                    this.response = response;
                    dialog.setVisible(false);
                    dialog.dispose();
                } else {
                    // Show error message
                }
            } catch (NumberFormatException e) {
                // Show error message
            }
        });
        dialog.setTitle(request.getInfo());
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void handleRequest(UseCathedralRequest request) {

    }

    public void handleRequest(ExpansionRequest request) {

    }

    public void handleRequest(UseUrbanAscendancyRequest request) {

    }

    public void handleRequest(StabilizationRequest request) {

    }

    public void handleRequest(SelectHolyIndulgencePaymentRequest request) {

    }

    public void handleRequest(UpgradeShipsRequest request) {

    }

    public void handleRequest(UseRenaissanceRequest request) {

    }

    public void handleRequest(SelectCivilWarLossesRequest request) {

    }

    public void handleRequest(AdjustShortageSurplusRequest request) {

    }

    public void handleRequest(SelectAreaRequest request) {

    }

    public void handleRequest(PurchaseAdvancesRequest request) {

    }

    public void handleRequest(SelectTargetCitiesRequest request) {

    }
}
