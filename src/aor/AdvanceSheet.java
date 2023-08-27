package aor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.*;

public class AdvanceSheet extends JPanel {

    private final double scale;
    protected final Dimension size;
    private final Image sheetImage;
    private Point cursor;

    private int scale(int c) {
        return (int) (scale * c + 0.5);
    }

    AdvanceSheet(String imagePath) {
        final ImageIcon icon = new ImageIcon(imagePath);
        final Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        scale = Math.min((double) rect.width * 0.9 / icon.getIconWidth(), (double) rect.height * 0.9 / icon.getIconHeight());
        final int sheetWidth = scale(icon.getIconWidth());
        final int sheetHeight = scale(icon.getIconHeight());
        size = new Dimension(sheetWidth, sheetHeight);
        sheetImage = ImageUtil.getScaledInstance(ImageUtil.toBufferedImage(icon.getImage()), sheetWidth, sheetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
                final Point point = e.getPoint();
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

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(sheetImage, 0, 0, null);
        if (cursor != null) {
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }
}
