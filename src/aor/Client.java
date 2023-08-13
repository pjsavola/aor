package aor;

import message.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class Client extends Board implements Runnable {
    private static int counter;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final Socket socket;
    private final int index;
    private final JFrame frame;
    private final boolean ai;
    private GameState gameState;
    private volatile Response response;

    public Client(JFrame frame, Socket socket, boolean ai) throws IOException {
        super(frame, "map.jpg");

        this.socket = socket;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
        index = counter++;
        this.frame = frame;
        this.ai = ai;
        if (!ai) {
            load(new File("map.dat"));
            frame.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        esc();
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            frame.setTitle("Age of Renaissance");
            frame.setContentPane(this);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);
            frame.pack();
            frame.setVisible(true);
        }
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
        if (ai) {
            return request.getDefaultResponse();
        }
        response = null;
        gameState = request.gameState;
        request.handleRequest(this);
        repaint();
        while (response == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return request.getDefaultResponse();
            }
        }
        return (U) response;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (gameState == null) return;

        // Render draw deck
        if (gameState.deckSize > 0) {
            g.setColor(Color.BLACK);
            final Rectangle bounds = getDrawDeckBounds();
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            final String cardCount = Integer.toString(gameState.deckSize);
            final int cardCountWidth = g.getFontMetrics().stringWidth(cardCount);
            g.drawString(cardCount, 960 - cardCountWidth / 2, 300);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Times New Roman", Font.BOLD, 24));
            StringBuilder epoch = new StringBuilder();
            epoch.append("I".repeat(Math.max(0, gameState.epoch)));
            final int epochWidth = g.getFontMetrics().stringWidth(epoch.toString());
            g.drawString(epoch.toString(), 960 - epochWidth / 2, 350);
        }

        // Render misery track
        final Map<Integer, Set<Capital>> miseryMap = new HashMap<>();
        for (PlayerState playerState : gameState.turnOrder) {
            if (playerState.capital == null) continue;
            final int miseryStep = playerState.chaos ? Player.miserySteps.length : playerState.misery;
            miseryMap.putIfAbsent(miseryStep, new HashSet<>());
            miseryMap.get(miseryStep).add(playerState.capital);
        }
        final Rectangle miseryBounds = getMiserySlotBounds();
        miseryMap.forEach((miseryStep, capitals) -> {
            int dx = 0;
            int dy = 0;
            if (miseryStep % 2 != 0) {
                dx -= miseryBounds.width * 18 / 10;
                dy += miseryBounds.height;
            }
            dy += miseryBounds.height * (miseryStep / 2) * 2;
            for (Capital capital : capitals) {
                g.setColor(Color.BLACK);
                g.drawRect(miseryBounds.x + dx, miseryBounds.y + dy, getTokenSize(), getTokenSize());
                g.setColor(capital.getColor());
                g.fillRect(miseryBounds.x + dx, miseryBounds.y + dy, getTokenSize(), getTokenSize());
                dx -= 2;
                dy -= 2;
            }
        });

        // Render turn order
        final Rectangle turnOrderBounds = getTurnOrderBounds();
        for (int i = 0; i < gameState.turnOrder.size(); ++i) {
            final PlayerState playerState = gameState.turnOrder.get(i);
            if (playerState.capital == null) continue;
            final int order = Server.getTurnOrderThreshold(i, gameState.turnOrder.size());
            int dx = 0;
            int dy = (order - 1) * turnOrderBounds.height * 24 / 10;
            g.setColor(Color.BLACK);
            g.drawRect(turnOrderBounds.x + dx, turnOrderBounds.y + dy, getTokenSize(), getTokenSize());
            g.setColor(playerState.capital.getColor());
            g.fillRect(turnOrderBounds.x + dx, turnOrderBounds.y + dy, getTokenSize(), getTokenSize());
        }

        // Render cities and tokens
        final int sz = getCitySize();
        final Map<Node, Map<Capital, Integer>> tokenMap = new HashMap<>();
        final Map<Node, Map<Capital, Integer>> newTokenMap = new HashMap<>();
        for (PlayerState playerState : gameState.turnOrder) {
            for (int i = 0; i < playerState.areas.size(); ++i) {
                final Node node = Node.nodeMap.get(playerState.areas.get(i));
                final int tokens = playerState.tokens.get(i);
                final Point p = node.getMiddle();
                if ((node.getSize() == tokens) && node.getSize() > 1) {
                    g.setColor(playerState.capital.getColor());
                    g.fillOval(p.x - sz / 2, p.y - sz / 2, sz, sz);
                    g.setColor(Color.WHITE);
                    g.setColor(Color.BLACK);
                    g.drawOval(p.x - sz / 2, p.y - sz / 2, sz, sz);
                } else {
                    tokenMap.putIfAbsent(node, new HashMap<>());
                    tokenMap.get(node).put(playerState.capital, tokens);
                }
            }
            for (int i = 0; i < playerState.newAreas.size(); ++i) {
                final Node node = Node.nodeMap.get(playerState.newAreas.get(i));
                final int newTokens = playerState.newTokens.get(i);
                final Point p = node.getMiddle();
                if (node.getSize() == newTokens && node.getSize() > 1) {
                    g.setColor(playerState.capital.getColor());
                    g.fillOval(p.x - sz / 2, p.y - sz / 2, sz, sz);
                    g.setColor(Color.WHITE);
                    final int whiteSz = sz * 3 / 4;
                    g.fillOval(p.x - whiteSz / 2, p.y - whiteSz / 2, whiteSz, whiteSz);
                    g.setColor(Color.BLACK);
                    g.drawOval(p.x - sz / 2, p.y - sz / 2, sz, sz);
                } else {
                    newTokenMap.putIfAbsent(node, new HashMap<>());
                    newTokenMap.get(node).put(playerState.capital, newTokens);
                }
            }
        }

        Node.nodeMap.values().forEach(node -> {
            final int size = getTokenSize();
            final int whiteSize = size * 3 / 4;
            final Map<Capital, Integer> tokens = tokenMap.get(node);
            final Map<Capital, Integer> newTokens = newTokenMap.get(node);
            final Point d = new Point(0, 0);
            if (tokens != null) tokens.forEach((capital, count) -> {
                g.setColor(capital.getColor());
                g.fillRect(node.getMiddle().x - size / 2 + d.x, node.getMiddle().y - size / 2 + d.y, size, size);
                g.setColor(Color.BLACK);
                g.drawRect(node.getMiddle().x - size / 2 + d.x, node.getMiddle().y - size / 2 + d.y, size, size);
                d.setLocation(d.x - 3, d.y - 3);
            });
            if (newTokens != null) newTokens.forEach((capital, count) -> {
                g.setColor(Color.BLACK);
                g.drawRect(node.getMiddle().x - size / 2 + d.x, node.getMiddle().y - size / 2 + d.y, size, size);
                g.setColor(Color.WHITE);
                g.fillRect(node.getMiddle().x - whiteSize / 2 + d.x, node.getMiddle().y - whiteSize / 2 + d.y, whiteSize, whiteSize);
                g.setColor(capital.getColor());
                g.fillRect(node.getMiddle().x - size / 2 + d.x, node.getMiddle().y - size / 2 + d.y, size, size);
                d.setLocation(d.x - 3, d.y - 3);
            });
        });
    }

    public void handleRequest(SelectCardRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Card> cards = request.getCards();
        if (request.optional) {
            final JButton button = new JButton("Pass");
            button.setForeground(Color.RED);
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
        final List<Capital> options = request.options;
        for (int i = 0; i < options.size(); ++i) {
            final Capital capital = options.get(i);
            final JButton button = new JButton(capital.name());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new CapitalResponse(capital);
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
            final Commodity commodity = options.get(i);
            final JButton button = new JButton(commodity.name());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new CommodityResponse(commodity);
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
            final Advance.Category category = categories.get(i);
            final JButton button = new JButton(category.name());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new CategoryResponse(category);
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
