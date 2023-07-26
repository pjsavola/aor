import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapEditor extends JPanel {

    private final List<Line> lines = new ArrayList<>();
    private final double scale;
    private final int mapWidth;
    private final int mapHeight;
    private final Dimension size;
    private final Image mapImage;


    private Point p;
    private Point cursor;

    MapEditor() {
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
                final Point c = snap(e.getPoint());
                final boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
                if (p == null) {
                    if (rightClick) {
                        lines.removeIf(l -> l.p1 == c || l.p2 == c);
                    } else {
                        p = c;
                    }
                } else if (p == c) {
                    p = null;
                } else {
                    lines.add(new Line(p, c, rightClick));
                    p = c;
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

    public void esc() {
        p = null;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(mapImage, 0, 0, null);
        Set<Point> renderedPoints = new HashSet<>();
        for (Line line : lines) {
            line.draw(g);
            g.setColor(Color.GREEN.darker());
            final int size = Line.tolerance;
            if (renderedPoints.add(line.p1)) g.fillOval(line.p1.x - size / 2, line.p1.y - size / 2, size, size);
            if (renderedPoints.add(line.p2)) g.fillOval(line.p2.x - size / 2, line.p2.y - size / 2, size, size);
        }
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
