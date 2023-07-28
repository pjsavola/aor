import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class MapEditor extends JPanel {

    private final JFrame frame;
    private final List<Line> lines = new ArrayList<>();
    private final List<Node> nodes = new ArrayList<>();
    private final double scale;
    private final int mapWidth;
    private final int mapHeight;
    private final Dimension size;
    private final Image mapImage;


    private Point p;
    private Point cursor;
    private List<Line> borders = new ArrayList<>();
    private Node selected;

    private static final Set<Point> pointCache = new HashSet<>();

    private Point p(int x, int y) {
        final Point p = new Point(x, y);
        if (pointCache.add(p)) {
            return p;
        }
        return pointCache.stream().filter(a -> a.equals(p)).findAny().orElse(null);
    }

    MapEditor(JFrame frame) {
        this.frame = frame;
        final ImageIcon icon = new ImageIcon("map.jpg");
        final Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        scale = Math.min((double) rect.width / icon.getIconWidth(), (double) rect.height / icon.getIconHeight());
        mapWidth = (int) (icon.getIconWidth() * scale);
        mapHeight = (int) (icon.getIconHeight() * scale);
        size = new Dimension(mapWidth, mapHeight);
        mapImage = getScaledInstance(toBufferedImage(icon.getImage()), mapWidth, mapHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        addMouseListener(new MouseListener() {
            private Point snap(Point c) {
                // Snap to nearby existing points
                for (Line line : lines) {
                    if (Line.nearby(line.p1, c)) {
                        return line.p1;
                    } else if (Line.nearby(line.p2, c)) {
                        return line.p2;
                    }
                }
                return c;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
                Line match = null;
                final Point a = e.getPoint();
                final Point c = snap(a);
                if (a == c && p == null) {
                    if (rightClick) {
                        if (selected != null) {
                            for (Node node : nodes) {
                                if (node.contains(c)) {
                                    if (!selected.supports(node)) {
                                        selected.addSupport(node);
                                        repaint();
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        for (Node node : nodes) {
                            if (node.contains(c)) {
                                if (node == selected) {
                                    selected = null;
                                } else {
                                    selected = node;
                                }
                                repaint();
                                return;
                            }
                        }
                    }
                }
                if (p == null) {
                    if (rightClick) {
                        if (!lines.removeIf(l -> l.p1 == c || l.p2 == c)) {
                            for (Line line : lines) {
                                final int x = (line.p1.x + line.p2.x) / 2;
                                final int y = (line.p1.y + line.p2.y) / 2;
                                if (Line.nearby(c, new Point(x, y))) {
                                    nodes.removeIf(n -> n.needsRemoval(line));
                                    lines.remove(line);
                                    break;
                                }
                            }
                        }
                    } else {
                        p = c;
                    }
                } else if (p == c) {
                    p = null;
                } else {
                    for (Line line : lines) {
                        if ((line.p1 == c && line.p2 == p) || (line.p2 == c && line.p1 == p)) {
                            match = line;
                            break;
                        }
                    }
                    if (match == null) {
                        lines.add(new Line(p, c, rightClick));
                    } else {
                        boolean duplicate = false;
                        for (Line line : borders) {
                            if ((line.p1 == c && line.p2 == p) || (line.p2 == c && line.p1 == p)) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            borders.add(match);
                            if (borders.size() > 2) {
                                final Line l1 = borders.get(0);
                                final Line l2 = borders.get(borders.size() - 1);
                                if (l1.p1 == l2.p1 || l1.p1 == l2.p2 || l1.p2 == l2.p1 || l1.p2 == l2.p2) {
                                    final JDialog dialog = new JDialog(frame);
                                    final JPanel panel = new JPanel();
                                    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                                    panel.setLayout(new GridLayout(6, 2));
                                    panel.add(new JLabel("Name"));
                                    final JTextField nameField = new JTextField();
                                    panel.add(nameField);
                                    panel.add(new JLabel("Size"));
                                    final JTextField sizeField = new JTextField();
                                    panel.add(sizeField);
                                    panel.add(new JLabel("Commodity"));
                                    final Node.Commodity[] commodityChoices = new Node.Commodity[Node.Commodity.values().length + 1];
                                    System.arraycopy(Node.Commodity.values(), 0, commodityChoices, 1, Node.Commodity.values().length);
                                    final JComboBox<Node.Commodity> commoditySelector = new JComboBox<>(commodityChoices);
                                    panel.add(commoditySelector);
                                    panel.add(new JLabel("Capital"));
                                    final Node.CityState[] capitalChoices = new Node.CityState[Node.CityState.values().length + 1];
                                    System.arraycopy(Node.CityState.values(), 0, capitalChoices, 1, Node.CityState.values().length);
                                    final JComboBox<Node.CityState> capitalSelector = new JComboBox<>(capitalChoices);
                                    panel.add(capitalSelector);
                                    panel.add(new JLabel("Region"));
                                    final JTextField regionField = new JTextField();
                                    panel.add(regionField);
                                    final JButton cancelButton = new JButton("Cancel");
                                    final JButton okButton = new JButton("Ok");
                                    cancelButton.addActionListener(l -> dialog.setVisible(false));
                                    okButton.addActionListener(l -> {
                                        final Node node = new Node();
                                        final String name = nameField.getText();
                                        final String size = sizeField.getText();
                                        final String region = regionField.getText();
                                        final Node.Commodity commodity = (Node.Commodity) commoditySelector.getSelectedItem();
                                        final Node.CityState capital = (Node.CityState) capitalSelector.getSelectedItem();
                                        node.init(borders, name, size.isEmpty() ? 0 : Integer.parseInt(size), commodity, capital, region.isEmpty() ? 0 : Integer.parseInt(region));
                                        nodes.add(node);
                                        dialog.setVisible(false);
                                    });
                                    panel.add(cancelButton);
                                    panel.add(okButton);
                                    dialog.setContentPane(panel);
                                    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    dialog.setLocationRelativeTo(frame);
                                    dialog.pack();
                                    dialog.setModal(true);
                                    dialog.setVisible(true);
                                    borders.clear();
                                    p = null;
                                    repaint();
                                    return;
                                }
                            }
                        }
                    }
                    p = c;
                }
                if (match == null) {
                    borders.clear();
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
                cursor = e.getPoint();
                if (p != null) {
                    repaint();
                }
            }
        });
    }

    public void save() {
        final JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
                for (Line line : lines) {
                    final int x1 = (int) (line.p1.x / scale + 0.5);
                    final int y1 = (int) (line.p1.y / scale + 0.5);
                    final int x2 = (int) (line.p2.x / scale + 0.5);
                    final int y2 = (int) (line.p2.y / scale + 0.5);
                    writer.println(x1 + " " + y1 + " " + x2 + " " + y2 + " " + (line.water ? 1 : 0));
                }
                writer.println("---");
                for (Node node : nodes) {
                    writer.println(node.serialize(lines, nodes));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.err.println("Wrote to " + file.getAbsolutePath());
        }
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

    public void load() {
        final JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            load(file);
        }
    }

    private int scale(int c) {
        return (int) (scale * c + 0.5);
    }

    public void esc() {
        p = null;
        borders.clear();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(mapImage, 0, 0, null);
        Set<Point> renderedPoints = new HashSet<>();
        for (Line line : lines) {
            line.draw(g, borders.contains(line));
        }
        g.setColor(Color.GREEN.darker());
        for (Line line : lines) {
            final int size = Line.tolerance;
            if (renderedPoints.add(line.p1)) g.fillOval(line.p1.x - size / 2, line.p1.y - size / 2, size, size);
            if (renderedPoints.add(line.p2)) g.fillOval(line.p2.x - size / 2, line.p2.y - size / 2, size, size);
        }
        if (selected != null) {
            selected.draw(g);
            final Set<Node> reachable = selected.getReachableNodes(2, false, false);
            for (Node node : reachable) {
                node.draw(g);
            }
            /*
            for (Node node : nodes) {
                if (selected.supports(node)) {
                    node.draw(g);
                }
            }*/
        }
        /*
        for (Node node : nodes) {
            node.draw(g);
        }*/
        if (p != null) {
            g.setColor(Color.RED);
            g.drawLine(p.x, p.y, cursor.x, cursor.y);
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
