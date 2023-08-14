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
import java.util.stream.Collectors;

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
    private final List<String> log = new ArrayList<>();
    private final LogPanel logPanel;
    private SelectTargetCitiesRequest pendingRequest;
    private SelectTargetCitiesResponse pendingResponse;

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
        logPanel = new LogPanel(log);
        logPanel.setHeight(size.height);
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                final Object message = ois.readObject();
                if (message instanceof Request) {
                    oos.writeObject(getResponse((Request<?>) message));
                } else if (message instanceof Notification) {
                    if (message instanceof LogEntryNotification) {
                        log.add(((LogEntryNotification) message).getText());
                        repaint(size.width - 200, size.height - logPanel.getHeight(), 200, logPanel.getHeight());
                    }
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
        if (gameState != null) {
            final int height = size.height - 100 * gameState.players.size();
            logPanel.setHeight(height);
        }
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
        final Rectangle bounds = getDrawDeckBounds();
        if (gameState.deckSize > 0) {
            g.setColor(Color.BLACK);
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
        for (PlayerState playerState : gameState.players) {
            if (playerState.capital == null) continue;
            final int miseryStep = playerState.chaos ? Player.miserySteps.length : playerState.misery;
            miseryMap.putIfAbsent(miseryStep, new HashSet<>());
            miseryMap.get(miseryStep).add(playerState.capital);
        }
        final Point miseryTrackLocation = getMiseryTrackLocation();
        miseryMap.forEach((miseryStep, capitals) -> {
            int dx = 0;
            int dy = 0;
            if (miseryStep % 2 != 0) {
                dx -= getTokenSize() * 18 / 10;
                dy += getTokenSize();
            }
            dy += getTokenSize() * (miseryStep / 2) * 2;
            for (Capital capital : capitals) {
                renderToken(g, capital, miseryTrackLocation.x + dx, miseryTrackLocation.y + dy, false, false);
                dx -= 2;
                dy -= 2;
            }
        });

        // Render turn order
        final Point turnOrderLocation = getTurnOrderLocation();
        for (int i = 0; i < gameState.turnOrder.length; ++i) {
            final PlayerState playerState = gameState.players.get(gameState.turnOrder[i]);
            if (playerState.capital == null) continue;
            final int order = Server.getTurnOrderThreshold(i, gameState.players.size());
            int dx = 0;
            int dy = (order - 1) * getTokenSize() * 24 / 10;
            renderToken(g, playerState.capital, turnOrderLocation.x + dx, turnOrderLocation.y + dy, false, false);
        }

        // Render cities, build data structures for tokens
        final int sz = getCitySize();
        final Map<Node, Map<Capital, Integer>> tokenMap = new HashMap<>();
        final Map<Node, Map<Capital, Integer>> newTokenMap = new HashMap<>();
        for (PlayerState playerState : gameState.players) {
            for (int i = 0; i < playerState.areas.size(); ++i) {
                final Node node = Node.nodeMap.get(playerState.areas.get(i));
                int tokens = playerState.tokens.get(i);
                if (pendingResponse != null && pendingResponse.getCities().contains(node.getName())) {
                    tokens = 1;
                }
                final Point p = node.getMiddle();
                if ((node.getSize() == tokens) && node.getSize() > 1) {
                    renderCity(g, playerState.capital, p.x, p.y, sz, false, true);
                } else {
                    tokenMap.putIfAbsent(node, new HashMap<>());
                    tokenMap.get(node).put(playerState.capital, tokens);
                }
            }
            for (int i = 0; i < playerState.newAreas.size(); ++i) {
                final Node node = Node.nodeMap.get(playerState.newAreas.get(i));
                int newTokens = playerState.newTokens.get(i);
                if (pendingResponse != null && pendingResponse.getCities().contains(node.getName())) {
                    newTokens = 1;
                }
                final Point p = node.getMiddle();
                if (node.getSize() == newTokens && node.getSize() > 1) {
                    renderCity(g, playerState.capital, p.x, p.y, sz, true, true);
                } else {
                    newTokenMap.putIfAbsent(node, new HashMap<>());
                    newTokenMap.get(node).put(playerState.capital, newTokens);
                }
            }
        }

        // Render tokens
        Node.nodeMap.values().forEach(node -> {
            final Map<Capital, Integer> tokens = tokenMap.get(node);
            final Map<Capital, Integer> newTokens = newTokenMap.get(node);
            final Point d = new Point(0, 0);
            if (tokens != null) tokens.forEach((capital, count) -> {
                renderToken(g, capital, node.getMiddle().x + d.x, node.getMiddle().y + d.y, false, true);
            });
            if (newTokens != null) newTokens.forEach((capital, count) -> {
                renderToken(g, capital, node.getMiddle().x + d.x, node.getMiddle().y + d.y, true, true);
                d.setLocation(d.x - 3, d.y - 3);
            });
        });

        // Render shipping
        final Map<Integer, Set<Capital>> shippingMap = new HashMap<>();
        for (PlayerState playerState : gameState.players) {
            if (playerState.capital == null) continue;
            int level = 0;
            if (Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.seaworthyVessels)) ++level;
            if (Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).anyMatch(a -> a == Advance.oceanNavigation)) ++level;
            final int totalLevel = level * 5 + playerState.shipLevel;
            shippingMap.putIfAbsent(totalLevel, new HashSet<>());
            shippingMap.get(totalLevel).add(playerState.capital);
        }
        shippingMap.forEach((totalLevel, capitals) -> {
            final int level = totalLevel % 5;
            if (level == 0) return;

            int x = getShippingLocation().x;
            int y = getShippingLocation().y;
            x += (level - 1) * getTokenSize() * 3;
            if (level > 2) x+= getTokenSize() * 2 / 3;
            y += (totalLevel / 5) * getTokenSize() * 6;

            int dx = 0;
            int dy = 0;
            int index = 0;
            for (Capital capital : capitals) {
                renderToken(g, capital, x + dx, y + dy, false, false);
                ++index;
                if (index > 2) {
                    dy  = 0;
                    dx += getTokenSize() + 3;
                } else {
                    dy += getTokenSize() + 3;
                }
            }
        });

        // Render resource production
        for (Commodity commodity : Commodity.values()) {
            final Map<Integer, Set<Capital>> productionMap = new HashMap<>();
            for (PlayerState playerState : gameState.players) {
                if (playerState.capital == null) continue;
                int count = 0;
                for (int i = 0; i < playerState.areas.size(); ++i) {
                    final Node node = Node.nodeMap.get(playerState.areas.get(i));
                    if (node.hasCommodity(commodity)) {
                        final int tokens = playerState.tokens.get(i);
                        if (tokens == node.getSize()) ++count;
                    }
                }
                for (int i = 0; i < playerState.newAreas.size(); ++i) {
                    final Node node = Node.nodeMap.get(playerState.newAreas.get(i));
                    if (node.hasCommodity(commodity)) {
                        final int tokens = playerState.newTokens.get(i);
                        if (tokens == node.getSize()) ++count;
                    }
                }
                productionMap.putIfAbsent(count, new HashSet<>());
                productionMap.get(count).add(playerState.capital);
            }
            productionMap.forEach((count, capitals) -> {
                int x = getCommodityTrackLocation().x + getTokenSize() * 5 / 2 * count;
                int y = getCommodityTrackLocation().y + getTokenSize() * 99 / 40 * commodity.ordinal();
                int dx = 0;
                int dy = 0;
                for (Capital capital : capitals) {
                    renderToken(g, capital, x + dx, y + dy, false, false);
                    dx -= 2;
                    dy -= 2;
                }
            });
        }

        // Render player infos
        int y = 0;
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        final int h = g.getFontMetrics().getHeight();
        for (PlayerState playerState : gameState.players) {
            if (playerState.capital == null) continue;

            //final int tokens = playerState.getAreas().entrySet().stream().filter(e -> e.getValue() < e.getKey().getSize() || e.getValue() == 1).map(Map.Entry::getValue).mapToInt(Integer::intValue).sum();
            //final int newTokens = playerState.getNewAreas().entrySet().stream().filter(e -> e.getValue() < e.getKey().getSize() || e.getValue() == 1).map(Map.Entry::getValue).mapToInt(Integer::intValue).sum();
            //final int remaining = Player.maxTokenCount - tokens - newTokens;
            final long cities = playerState.getAreas().entrySet().stream().filter(e -> e.getValue() == e.getKey().getSize() && e.getValue() > 1).count();
            final long newCities = playerState.getNewAreas().entrySet().stream().filter(e -> e.getValue() == e.getKey().getSize() && e.getValue() > 1).count();
            final int points = playerState.getAdvances().map(Advance::getBaseCost).mapToInt(Integer::intValue).sum() + playerState.cash - (playerState.chaos ? 1000 : Player.miserySteps[playerState.misery]);
            int x = size.width - 195;
            int dy = h - 2;
            g.setColor(playerState.capital.getColor());
            g.fillRect(size.width - 200, y, 200, 100);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(playerState.capital.name(), x, y + dy);
            dy += h;
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Cash: " + playerState.cash + " (" + playerState.writtenCash + " written)", x, y + dy);
            dy += h;
            g.drawString("Tokens: " + (playerState.remainingTokens + playerState.usableTokens) + " (" + playerState.usableTokens + " usable)", x, y + dy);
            dy += h;
            g.drawString("Cities: " + (cities + newCities) + " (" + newCities + " new)", x, y + dy);
            dy += h;
            g.drawString("Cards: " + playerState.numberOfCards, x, y + dy);
            dy += h;
            g.drawString("Advances: " + playerState.advances.length + " / " + Advance.allAdvances.size(), x, y + dy);
            dy += h;
            g.drawString("Points: " + points, x, y + dy);
            y += 100;
        }

        // Patronage queue
        {
            int dx = 0;
            int dy = 0;
            final Point patronageQueue = getPatronageQueueLocation();
            for (int i = 0; i < gameState.patronageCards.length; ++i) {
                final Card card = Card.allCards.get(gameState.patronageCards[i]);
                final int uses = gameState.patronageUsesRemaining[i];
                final Capital owner = gameState.players.get(gameState.patronageOwners[i]).capital;
                card.render(g, patronageQueue.x + dx, patronageQueue.y + dy, bounds.width, bounds.height);
                dx += 6;
                dy += 6;
            }
        }

        // Played cards
        {
            int dx = 0;
            int dy = 0;
            final Point playedCards = getPlayedCardsLocation();
            for (int i = 0; i < gameState.playedCards.length; ++i) {
                final Card card = Card.allCards.get(gameState.playedCards[i]);
                card.render(g, playedCards.x + dx, playedCards.y + dy, bounds.width, bounds.height);
                dx -= 3;
                dy -= 3;
            }
        }

        // Log panel
        logPanel.paint(g, size.width - 200, size.height - logPanel.getHeight());
    }

    public void handleRequest(SelectCardRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Card> cards = request.getCards();
        panel.setLayout(new GridLayout(cards.size() / 5 + 1, Math.min(5, cards.size())));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        final Rectangle bounds = getDrawDeckBounds();
        for (int i = 0; i < cards.size(); ++i) {
            final int index = i;
            final Card card = cards.get(i);
            final JButton button = new JButton(card.getName()) {
                @Override
                public void paint(Graphics g) {
                    card.render(g, 0, 0, bounds.width, bounds.height);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(bounds.width + 5, bounds.height);
                }
            };
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new IntegerResponse(index);
            });
            panel.add(button);
        }
        if (request.optional) {
            final JPanel panelWithButton = new JPanel();
            panelWithButton.setLayout(new BoxLayout(panelWithButton, BoxLayout.Y_AXIS));
            final JButton button = new JButton("Pass");
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = request.getDefaultResponse();
            });
            panelWithButton.add(panel);
            panelWithButton.add(button);
            showDialog(dialog, panelWithButton, request.getInfo());
        } else {
            showDialog(dialog, panel, request.getInfo());
        }
    }

    public void handleRequest(SelectCapitalRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Capital> options = request.options;
        for (final Capital capital : options) {
            final JPanel capitalPanel = new JPanel();
            capitalPanel.setLayout(new BoxLayout(capitalPanel, BoxLayout.Y_AXIS));
            final JButton cityToken = new JButton() {
                @Override
                public void paint(Graphics g) {
                    renderCity(g, capital, 0, 0, getCitySize(), false, false);
                }
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(getCitySize(), getCitySize());
                }
            };
            capitalPanel.add(cityToken);
            final JButton button = new JButton(capital.name());
            button.addActionListener(l -> {
                dialog.setVisible(false);
                dialog.dispose();
                response = new CapitalResponse(capital);
            });
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            capitalPanel.add(button);
            panel.add(capitalPanel);
        }
        showDialog(dialog, panel, request.getInfo());
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
        showDialog(dialog, panel, request.getInfo());
    }

    public void handleRequest(SelectCategoryRequest request) {
        final JDialog dialog = new JDialog(frame, false);
        final JPanel panel = new JPanel();
        final List<Advance.Category> categories = request.options;
        {
            final JButton button = new JButton("Pass");
            button.setForeground(Color.RED);
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
        showDialog(dialog, panel, request.getInfo());
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
        showDialog(dialog, panel, request.getInfo());
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
        showDialog(dialog, panel, request.getInfo());
    }

    public void handleRequest(UseCathedralRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(ExpansionRequest request) {
        response = request.getDefaultResponse();
    }

    public void handleRequest(UseUrbanAscendancyRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(StabilizationRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(SelectHolyIndulgencePaymentRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(UpgradeShipsRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(UseRenaissanceRequest request) {
        response = request.getDefaultResponse();
    }

    public void handleRequest(SelectCivilWarLossesRequest request) {
        getBooleanResponse(request);
    }

    public void handleRequest(AdjustShortageSurplusRequest request) {
        response = request.getDefaultResponse();
    }

    public void handleRequest(SelectAreaRequest request) {
        response = request.getDefaultResponse();
    }

    public void handleRequest(PurchaseAdvancesRequest request) {
        response = request.getDefaultResponse();
    }

    public void handleRequest(SelectTargetCitiesRequest request) {
        pendingRequest = request;
    }

    private void showDialog(JDialog dialog, JPanel panel, String title) {
        dialog.setTitle(title);
        dialog.setContentPane(panel);
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    protected void clicked(Node node) {
        if (pendingRequest != null) {
            if (node.isInNewWorld() && pendingRequest.newWorldLimit == 0) return;
            if (node.isInAsia() && pendingRequest.asiaLimit == 0) return;
            if (pendingRequest.count == 0) return;

            if (pendingRequest.options.removeIf(n -> n.equals(node.getName()))) {
                if (node.isInNewWorld()) --pendingRequest.newWorldLimit;
                if (node.isInAsia()) --pendingRequest.asiaLimit;
                --pendingRequest.count;
                if (pendingResponse == null) {
                    pendingResponse = new SelectTargetCitiesResponse();
                }
                pendingResponse.addCity(node.getName());
                if (pendingRequest.count == 0) {
                    response = pendingResponse;
                    pendingResponse = null;
                    pendingRequest = null;
                }
                repaint();
            }
        }
    }

    @Override
    protected boolean shouldHighlight(Node node) {
        if (pendingRequest != null) {
            return pendingRequest.options.stream().anyMatch(n -> n.equals(node.getName()));
        }
        return false;
    }

    private void getBooleanResponse(Request request) {
        JDialog dialog = new JDialog();
        JPanel panel = new JPanel();
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");
        yesButton.addActionListener(l -> {
            this.response = new BooleanResponse(true);
            dialog.setVisible(false);
            dialog.dispose();
        });
        noButton.addActionListener(l -> {
            this.response = new BooleanResponse(false);
            dialog.setVisible(false);
            dialog.dispose();
        });
        showDialog(dialog, panel, request.getInfo());
    }
}
