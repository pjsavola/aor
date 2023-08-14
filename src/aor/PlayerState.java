package aor;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class PlayerState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Capital capital;
    public int numberOfCards;
    public int cash;
    public int writtenCash;
    public int misery;
    public boolean chaos;
    public int cathedralUsed;
    public int[] advances;
    public int shipLevel;
    public int usableTokens;
    public int remainingTokens;
    public List<String> areas = new ArrayList<>();
    public List<Integer> tokens = new ArrayList<>();
    public List<String> newAreas = new ArrayList<>();
    public List<Integer> newTokens = new ArrayList<>();
    public List<Integer> weapons = new ArrayList<>();

    public Stream<Advance> getAdvances() {
        return Arrays.stream(advances).mapToObj(a -> Advance.allAdvances.get(a));
    }

    public Map<Node, Integer> getAreas() {
        final Map<Node, Integer> areaMap = new HashMap<>(areas.size());
        for (int i = 0; i < areas.size(); ++i) {
            areaMap.put(Node.nodeMap.get(areas.get(i)), tokens.get(i));
        }
        return areaMap;
    }

    public Map<Node, Integer> getNewAreas() {
        final Map<Node, Integer> areaMap = new HashMap<>(newAreas.size());
        for (int i = 0; i < newAreas.size(); ++i) {
            areaMap.put(Node.nodeMap.get(newAreas.get(i)), newTokens.get(i));
        }
        return areaMap;
    }
}
