import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Node {
    public enum Commodity { STONE, WOOL, TIMBER, GRAIN, CLOTH, WINE, METAL, FUR, SILK, SPICE, GOLD, IVORY };
    public enum CityState { VENICE, GENOA, BARCELONA, PARIS, LONDON, HAMBURG };

    private List<Line> borders;
    private Polygon polygon;
    private String name;
    private int size;
    private Commodity commodity;
    private CityState capital;
    private final Set<Node> supports = new HashSet<>();

    public void init(List<Line> borders, String name, int size, Commodity commodity) {
        this.borders = new ArrayList<>(borders);
        this.name = name;
        this.size = size;
        this.commodity = commodity;
        initPolygon();
    }

    private void initPolygon() {
        final int corners = borders.size();
        final int[] xPoints = new int[corners];
        final int[] yPoints = new int[corners];
        for (int i = 0; i < corners; ++i) {
            xPoints[i] = borders.get(i).p1.x;
            yPoints[i] = borders.get(i).p1.y;
        }
        polygon = new Polygon(xPoints, yPoints, corners);
    }

    public void addCapital(CityState capital) {
        if (size > 1) {
            this.capital = capital;
        }
    }

    public void addSupport(Node node) {
        if (size == 1) {
            supports.add(node);
        }
    }

    public void removeSupport(Node node) {
        supports.remove(node);
    }

    public boolean supports(Node node) {
        return supports.contains(node);
    }

    public CityState getCapital() {
        return capital;
    }

    public boolean contains(Point p) {
        return polygon.contains(p);
    }

    public String serialize(List<Line> allLines, List<Node> allNodes) {
        final List<String> s = new ArrayList<>();
        s.add(String.valueOf(borders.size()));
        for (Line border : borders) s.add(String.valueOf(allLines.indexOf(border)));
        s.add(name);
        s.add(String.valueOf(size));
        s.add(String.valueOf(commodity == null ? 0 : commodity.ordinal() + 1));
        s.add(String.valueOf(capital == null ? 0 : capital.ordinal() + 1));
        s.add(String.valueOf(supports.size()));
        for (Node support : supports) s.add(String.valueOf(allNodes.indexOf(support)));
        return String.join(" ", s);
    }

    public static void initFromString(Node node, String str, List<Line> allLines, List<Node> allNodes) {
        int idx = 0;
        final String[] s = str.split(" ");
        int borderCount = Integer.parseInt(s[idx++]);
        final List<Line> borders = new ArrayList<>(borderCount);
        while (borderCount-- > 0) borders.add(allLines.get(Integer.parseInt(s[idx++])));
        final String name = s[idx++];
        final int size = Integer.parseInt(s[idx++]);
        final int commodityIdx = Integer.parseInt(s[idx++]);
        final Commodity commodity = commodityIdx == 0 ? null : Commodity.values()[commodityIdx - 1];
        final int capitalIdx = Integer.parseInt(s[idx++]);
        final CityState capital = capitalIdx == 0 ? null : CityState.values()[capitalIdx - 1];
        node.init(borders, name, size, commodity);
        node.addCapital(capital);
        int supportCount = Integer.parseInt(s[idx++]);
        while (supportCount-- > 0) node.addSupport(allNodes.get(Integer.parseInt(s[idx++])));
    }
}
