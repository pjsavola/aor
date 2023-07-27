import java.awt.*;

public class Line {

    final Point p1;
    final Point p2;
    final boolean water;
    static final int tolerance = 8;

    public Line(Point p1, Point p2, boolean water) {
        this.p1 = p1;
        this.p2 = p2;
        this.water = water;
    }

    public static boolean nearby(Point p1, Point p2) {
        final int dx = p1.x - p2.x;
        final int dy = p1.y - p2.y;
        return dx * dx + dy * dy < tolerance * tolerance;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        final Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.setColor(water ? Color.WHITE : Color.RED);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        g2d.setStroke(oldStroke);
    }
}