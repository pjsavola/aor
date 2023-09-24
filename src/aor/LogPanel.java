package aor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
        final FontMetrics metrics = g.getFontMetrics();
        StringBuilder leftovers = new StringBuilder();
        for (int i = log.size() - 1; i >= 0; ) {
            String txt;
            if (leftovers.isEmpty()) {
                txt = log.get(i);
            } else {
                txt = leftovers.toString();
                txt = txt.substring(0, txt.length() - 1); // remove last whitespace
                leftovers.setLength(0);
            }
            while (metrics.stringWidth(txt) > 195) {
                final int idx = txt.lastIndexOf(" ");
                if (idx == -1) break;
                leftovers.insert(0, txt.substring(idx + 1) + " ");
                txt = txt.substring(0, idx);
            }
            g.drawString(txt, x + dx, y + dy);
            dy += 15;
            if (dy > height) break;
            if (leftovers.length() == 0) --i;
        }
    }
}
