package aor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

public class Board extends JPanel {

    private final JFrame frame;
    private final List<Line> lines = new ArrayList<>();
    private final List<Node> nodes = new ArrayList<>();
    private final double scale;
    private final Dimension size;
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

    Board(JFrame frame) {
        this.frame = frame;
        final ImageIcon icon = new ImageIcon("map.jpg");
        final Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        scale = Math.min((double) rect.width / icon.getIconWidth(), (double) rect.height / icon.getIconHeight());
        final int mapWidth = (int) (icon.getIconWidth() * scale);
        final int mapHeight = (int) (icon.getIconHeight() * scale);
        size = new Dimension(mapWidth, mapHeight);
        mapImage = getScaledInstance(toBufferedImage(icon.getImage()), mapWidth, mapHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
                final Point point = e.getPoint();
                for (Node node : nodes) {
                    if (node.contains(point)) {
                        System.err.println("Clicked " + node.getName());
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
        lines.clear();
        lines.addAll(newLines);
        nodes.clear();
        nodes.addAll(newNodes);
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
        Set<Point> renderedPoints = new HashSet<>();
        g.setColor(Color.GREEN.darker());
        if (cursor != null) {
            for (Node node : nodes) {
                if (node.contains(cursor)) {
                    node.draw(g);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
