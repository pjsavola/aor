import java.awt.*;

public class Line {

    final Point p1;
    final Point p2;
    private final boolean water;
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
        g.setColor(water ? Color.BLUE : Color.BLACK);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
}