package aor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LogPanel {
    private final List<String> log;
    private int height;

    public LogPanel(final List<String> log) {
        this.log = log;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void paint(Graphics g, int x, int y) {
        int dx = 5;
        int dy = 15;
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = log.size() - 1; i >= 0; --i) {
            String txt = log.get(i);
            g.drawString(txt, x + dx, y + dy);
            dy += 15;
            if (dy > height) break;
        }
    }
}
