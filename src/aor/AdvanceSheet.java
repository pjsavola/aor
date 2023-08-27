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
    private final Client client;

    private int scale(int c) {
        return (int) (scale * c + 0.5);
    }

    AdvanceSheet(String imagePath, Client client) {
        this.client = client;
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
        final int boxSize = scale(26);
        final int offsetX = scale(12);
        final int[] offsetY = { scale(80), scale(310), scale(530), scale(790), scale(1010), scale(1230) };
        g.drawImage(sheetImage, 0, 0, null);
        int x = offsetX;
        for (Capital capital : Capital.values()) {
            final List<Advance> advances = client.getAdvances(capital);
            int y = offsetY[0];
            for (int i = 0; i < Advance.allAdvances.size(); ++i) {
                final Advance advance = Advance.allAdvances.get(i);
                if (i > 0 && advance.getCategory() != Advance.allAdvances.get(i - 1).getCategory()) {
                    y = offsetY[advance.getCategory().ordinal()];
                }
                if (advances.contains(advance)) {
                    final int deltaX = advance.getCategory().ordinal() > 2 ? scale(5) : 0;
                    g.setColor(capital.getColor());
                    g.fillRect(x + deltaX, y, boxSize, boxSize);
                }
                y += boxSize * 24 / 20;
            }
            x += boxSize * 23 / 20;
        }
        if (cursor != null) {
            x = offsetX;
            for (Capital capital : Capital.values()) {
                g.setColor(capital.getColor());
                int y = offsetY[0];
                for (int i = 0; i < Advance.allAdvances.size(); ++i) {
                    final Advance advance = Advance.allAdvances.get(i);
                    if (i > 0 && advance.getCategory() != Advance.allAdvances.get(i - 1).getCategory()) {
                        y = offsetY[advance.getCategory().ordinal()];
                    }
                    final int deltaX = advance.getCategory().ordinal() > 2 ? scale(5) : 0;
                    if (cursor.x >= x + deltaX && cursor.y >= y && cursor.x < x + deltaX + boxSize && cursor.y < y + boxSize) {
                        g.fillRect(x + deltaX, y, boxSize, boxSize);
                        System.err.println(advance.name);
                    }
                    y += boxSize * 24 / 20;
                }
                x += boxSize * 23 / 20;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }
}
