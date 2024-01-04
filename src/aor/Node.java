package aor;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Node {
    ;

    public static Map<String, Node> nodeMap = new HashMap<>();
    private List<Line> borders;
    private Polygon polygon;
    private String name;
    private int size;
    private Commodity commodity;
    private List<Commodity> specialCommodities;
    private Capital capital;
    private int region;
    private final Set<Node> supports = new HashSet<>();
    private final Set<Node> supportNodes = new HashSet<>();
    private Point middle;

    public void init(List<Line> borders, String name, int size, Commodity commodity, Capital capital, int region) {
        this.borders = new ArrayList<>(borders);
        this.name = name;
        this.size = size;
        this.commodity = commodity;
        this.capital = capital;
        this.region = region;
        initPolygon();
        for (Line line : borders) line.nodes.add(this);
        switch (name) {
            case "East Indies" -> specialCommodities = List.of(Commodity.SILK, Commodity.SPICE);
            case "China" -> specialCommodities = List.of(Commodity.SILK, Commodity.SPICE);
            case "India" -> specialCommodities = List.of(Commodity.GOLD, Commodity.SPICE);
            case "North America" -> specialCommodities = List.of(Commodity.GRAIN, Commodity.CLOTH, Commodity.FUR);
            case "South America" -> specialCommodities = List.of(Commodity.METAL, Commodity.SPICE, Commodity.GOLD);
        }
        nodeMap.put(name, this);
    }

    private void initPolygon() {
        final int corners = borders.size();
        final int[] xPoints = new int[corners];
        final int[] yPoints = new int[corners];
        for (int i = 0; i < corners; ++i) {
            final Point p1 = borders.get(i).p1;
            final Point p2 = borders.get(i).p2;
            final Point p;
            if (i == 0) {
                final Point n1 = borders.get(borders.size() - 1).p1;
                final Point n2 = borders.get(borders.size() - 1).p2;
                if (p1 == n1 || p1 == n2) {
                    p = p2;
                } else {
                    p = p1;
                }
            } else if (xPoints[i - 1] == p1.x && yPoints[i - 1] == p1.y) {
                p = p2;
            } else {
                p = p1;
            }
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }
        polygon = new Polygon(xPoints, yPoints, corners);
        int totalX = 0;
        int totalY = 0;
        int totalLen = 0;
        for (int i = 0; i < corners; ++i) {
            int x1 = xPoints[i];
            int y1 = yPoints[i];
            int x2 = xPoints[(i + 1) % xPoints.length];
            int y2 = yPoints[(i + 1) % yPoints.length];
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;
            int dx = x1 - x2;
            int dy = y1 - y2;
            int len = (int) (Math.sqrt(dx * dx + dy * dy) + 0.5);
            totalX += len * midX;
            totalY += len * midY;
            totalLen += len;
        }
        middle = new Point(totalX / totalLen, totalY / totalLen);
    }

    public String getName() {
        return name;
    }

    public Capital getCapital() {
        return capital;
    }

    public int getSize() {
        return size;
    }

    public int getRegion() {
        return region;
    }

    public boolean hasCommodity(Commodity commodity) {
        if (commodity != null) return this.commodity == commodity;
        return specialCommodities != null && specialCommodities.contains(commodity);
    }

    public boolean isCoastal() {
        for (Line line : borders) {
            if (line.water) return true;
        }
        return false;
    }

    public boolean isInNewWorld() {
        return isInNewWorld(name);
    }

    public static boolean isInNewWorld(String name) {
        return name.endsWith("America");
    }

    public boolean isInAsia() {
        return isInAsia(name);
    }

    public static boolean isInAsia(String name) {
        return name.equals("East Indies") || name.equals("China") || name.equals("India");
    }

    public boolean isAccessible(Set<Advance> advances) {
        if (region == 5) return advances.contains(Advance.overlandEast);
        else if (isInAsia()) return advances.contains(Advance.oceanNavigation);
        else if (isInNewWorld()) return advances.contains(Advance.newWorld);
        return true;
    }

    public boolean needsRemoval(Line line) {
        for (Line border : borders) {
            if (border == line) return true;
        }
        return false;
    }

    public void addSupport(Node node) {
        if (size == 1) {
            supports.add(node);
            node.supportNodes.add(this);
        }
    }

    public void removeSupport(Node node) {
        supports.remove(node);
        node.supportNodes.remove(this);
    }

    public boolean supports(Node node) {
        return supports.contains(node);
    }

    public Set<Node> getSupportNodes() {
        return supportNodes;
    }

    public boolean contains(Point p) {
        return polygon.contains(p);
    }

    private static class Work {
        private final Node node;
        private final int distance;
        private final Line border;
        private final boolean heavensUsed;
        private Work(Node node, int distance, Line border, boolean heavensUsed) {
            this.node = node;
            this.distance = distance;
            this.border = border;
            this.heavensUsed = heavensUsed;
        }
    }

    public Set<Node> getReachableNodes(int range, boolean useShips, boolean useHeavens, Set<Node> blockedNodes, int playerCount) {
        final Map<Line, Integer> borderDistances = new HashMap<>();
        final Map<Line, Integer> borderDistancesWithHeavens = new HashMap<>();
        final Deque<Work> work = new ArrayDeque<>();
        final Set<Node> result = new HashSet<>();
        work.addLast(new Work(this, 0, null, false));
        while (!work.isEmpty()) {
            final Work w = work.removeFirst();
            final Node node = w.node;
            final int distance = w.distance;
            final Line source = w.border;
            final int size = node.borders.size();
            for (int i = 0; i < size; ++i) {
                final Line border = node.borders.get(i);
                if (border == source) continue;
                if (border.water != useShips) continue;

                if (source != null) {
                    boolean reachable = false;
                    for (int j = i + 1; j < i + size; ++j) {
                        final Line otherBorder = node.borders.get(j % size);
                        if (!otherBorder.water) {
                            break;
                        }
                        if (otherBorder == source) {
                            reachable = true;
                            break;
                        }
                    }
                    if (!reachable) {
                        for (int j = i - 1; j > i - size; --j) {
                            final Line otherBorder = node.borders.get((j + size) % size);
                            if (!otherBorder.water) {
                                break;
                            }
                            if (otherBorder == source) {
                                reachable = true;
                                break;
                            }
                        }
                    }
                    if (!reachable) {
                        continue;
                    }
                }
                for (Node neighbor : border.nodes) {
                    if (neighbor == node) continue;

                    final boolean water = neighbor.commodity == null && neighbor.size == 0;
                    if (!water && 6 - neighbor.region >= playerCount) continue;

                    boolean heavensUsed = w.heavensUsed;
                    if (water) {
                        if (!useHeavens || w.heavensUsed) continue;
                        heavensUsed = true;
                    }
                    if (!borderDistances.containsKey(border) && (!heavensUsed || !borderDistancesWithHeavens.containsKey(border))) {
                        final Map<Line, Integer> map = heavensUsed ? borderDistancesWithHeavens : borderDistances;
                        map.put(border, distance + 1);
                        if (range > distance) {
                            if (!blockedNodes.contains(neighbor)) {
                                work.add(new Work(neighbor, distance + 1, useShips ? border : null, heavensUsed));
                            }
                            result.add(neighbor);
                        }
                    }
                }
            }
        }
        return result;
    }

    public void draw(Graphics g) {
        g.setColor(new Color(0x44FFFFFF, true));
        g.fillPolygon(polygon);
        //g.setColor(Color.BLACK);
        //g.fillOval(middle.x, middle.y, 10, 10);
    }

    @Override
    public String toString() {
        return name + " (" + size + ") " + commodity + " " + (capital == null ? "" : capital) + " Region " + region;
    }

    public String serialize(List<Line> allLines, List<Node> allNodes) {
        final List<String> s = new ArrayList<>();
        s.add(String.valueOf(borders.size()));
        for (Line border : borders) s.add(String.valueOf(allLines.indexOf(border)));
        s.add(name);
        s.add(String.valueOf(size));
        s.add(String.valueOf(commodity == null ? 0 : commodity.ordinal() + 1));
        s.add(String.valueOf(capital == null ? 0 : capital.ordinal() + 1));
        s.add(String.valueOf(region));
        s.add(String.valueOf(supports.size()));
        for (Node support : supports) s.add(String.valueOf(allNodes.indexOf(support)));
        return String.join(",", s);
    }

    public static void initFromString(Node node, String str, List<Line> allLines, List<Node> allNodes) {
        int idx = 0;
        final String[] s = str.split(",");
        int borderCount = Integer.parseInt(s[idx++]);
        final List<Line> borders = new ArrayList<>(borderCount);
        while (borderCount-- > 0) borders.add(allLines.get(Integer.parseInt(s[idx++])));
        final String name = s[idx++];
        final int size = Integer.parseInt(s[idx++]);
        final int commodityIdx = Integer.parseInt(s[idx++]);
        final Commodity commodity = commodityIdx == 0 ? null : Commodity.values()[commodityIdx - 1];
        final int capitalIdx = Integer.parseInt(s[idx++]);
        final Capital capital = capitalIdx == 0 ? null : Capital.values()[capitalIdx - 1];
        final int region = Integer.parseInt(s[idx++]);
        node.init(borders, name, size, commodity, capital, region);
        int supportCount = Integer.parseInt(s[idx++]);
        while (supportCount-- > 0) node.addSupport(allNodes.get(Integer.parseInt(s[idx++])));
    }

    public Point getMiddle() {
        return middle;
    }
}
