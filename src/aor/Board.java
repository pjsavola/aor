package aor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.List;
import java.util.*;

public class Board extends JPanel {

    private final JFrame frame;
    private final double scale;
    protected final Dimension size;
    private final Image mapImage;

    private Point cursor;

    private static final Set<Point> pointCache = new HashSet<>();

    private Point p(int x, int y) {
        final Point p = new Point(x, y);
        if (pointCache.add(p)) {
            return p;
        }
        return pointCache.stream().filter(a -> a.equals(p)).findAny().orElse(null);
    }

    Board(JFrame frame, String imagePath) {
        this.frame = frame;
        final ImageIcon icon = new ImageIcon(imagePath);
        final Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        scale = Math.min((double) rect.width / icon.getIconWidth(), (double) rect.height / icon.getIconHeight());
        final int mapWidth = scale(icon.getIconWidth());
        final int mapHeight = scale(icon.getIconHeight());
        size = new Dimension(mapWidth + 200, mapHeight);
        mapImage = ImageUtil.getScaledInstance(ImageUtil.toBufferedImage(icon.getImage()), mapWidth, mapHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
                final Point point = e.getPoint();
                for (Node node : Node.nodeMap.values()) {
                    if (node.contains(point)) {
                        clicked(node);
                    }
                }
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (cursor == null || !cursor.equals(e.getPoint())) {
                    cursor = e.getPoint();
                    repaint();
                }
            }
        });
    }

    public void load(File file) {
        final List<Line> newLines = new ArrayList<>();
        final List<Node> newNodes = new ArrayList<>();
        final List<String> nodeData = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            boolean readingNodes = false;
            String str;
            while (scanner.hasNextLine()) {
                str = scanner.nextLine();
                if ("---".equals(str)) {
                    readingNodes = true;
                    continue;
                }
                if (readingNodes) {
                    newNodes.add(new Node());
                    nodeData.add(str);
                } else {
                    final int[] s = Arrays.stream(str.split(" ")).mapToInt(Integer::parseInt).toArray();
                    final Line line = new Line(p(scale(s[0]), scale(s[1])), p(scale(s[2]), scale(s[3])), s[4] == 1);
                    newLines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < newNodes.size(); ++i) {
            Node.initFromString(newNodes.get(i), nodeData.get(i), newLines, newNodes);
        }
        repaint();
        System.err.println("Loaded from " + file.getAbsolutePath());
    }

    private int scale(int c) {
        return (int) (scale * c + 0.5);
    }

    public void esc() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(mapImage, 0, 0, null);
        if (cursor != null) {
            for (Node node : Node.nodeMap.values()) {
                if (shouldHighlight(node) && node.contains(cursor)) {
                    node.draw(g);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    protected Rectangle getDrawDeckBounds() {
        return new Rectangle(scale(2935), scale(839), scale(323), scale(516));
    }

    protected Point getMiseryTrackLocation() {
        return new Point(scale(3432), scale(471));
    }

    protected Point getTurnOrderLocation() {
        return new Point(scale(3616), scale(505));
    }

    protected Point getShippingLocation() {
        return new Point(scale(129), scale(194));
    }

    protected Point getCommodityTrackLocation() {
        return new Point(scale(97), scale(1871));
    }

    protected Point getPatronageQueueLocation() {
        return new Point(scale(185), scale(1102));
    }

    protected Point getPlayedCardsLocation() {
        return new Point(scale(4250), scale(395));
    }

    protected int getCitySize() {
        return scale(80);
    }

    protected int getTokenSize() {
        return scale(45);
    }

    protected void renderToken(Graphics g, Capital capital, int x, int y, boolean newToken, boolean centralized) {
        final int size = getTokenSize();
        final int finalX = centralized ? x - size / 2 : x;
        final int finalY = centralized ? y - size / 2 : y;
        g.setColor(capital.getColor());
        g.fillRect(finalX, finalY, size, size);
        if (newToken) {
            final int whiteSize = size * 3 / 4;
            final int finalWhiteX = centralized ? x - whiteSize / 2 : x;
            final int finalWhiteY = centralized ? y - whiteSize / 2 : y;
            g.setColor(Color.WHITE);
            g.fillRect(finalWhiteX, finalWhiteY, whiteSize, whiteSize);
        }
        g.setColor(Color.BLACK);
        g.drawRect(finalX, finalY, size, size);
        renderLetter(g, capital, x, y, centralized ? 0 : size);
    }

    protected void renderCity(Graphics g, Capital capital, int x, int y, int size, boolean newCity, boolean centralized) {
        g.setColor(capital.getColor());
        final int finalX = x - (centralized ? size / 2 : 0);
        final int finalY = y - (centralized ? size / 2 : 0);
        g.fillOval(finalX, finalY, size, size);
        if (newCity) {
            g.setColor(Color.WHITE);
            final int whiteSz = size * 3 / 4;
            final int finalWhiteX = x - (centralized ? whiteSz / 2 : 0);
            final int finalWhiteY = y - (centralized ? whiteSz / 2 : 0);
            g.fillOval(finalWhiteX, finalWhiteY, whiteSz, whiteSz);
        }
        g.setColor(Color.BLACK);
        g.drawOval(finalX, finalY, size, size);
        renderLetter(g, capital, x, y, centralized ? 0 : size);
    }

    protected void renderLetter(Graphics g, Capital capital, int x, int y, int size) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        final String letter = capital.name().substring(0, 1);
        g.drawString(letter, x + (size - g.getFontMetrics().stringWidth(letter)) / 2, y + (size + g.getFontMetrics().getHeight()) / 2 - 2);
    }

    protected void clicked(Node node) {
    }

    protected boolean shouldHighlight(Node node) {
        return false;
    }
}
