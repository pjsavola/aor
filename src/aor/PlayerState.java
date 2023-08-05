package aor;

import java.io.Serializable;
import java.util.*;

public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Node.CityState capital;
    public int numberOfCards;
    public int cash;
    public int writtenCash;
    public int misery;
    public int[] advances;
    public List<Integer> ownedPatronageCards = new ArrayList<>();
    public Map<Node, Integer> areas = new HashMap<>();
    public Map<Node, Integer> newAreas = new HashMap<>();
    public Set<Integer> weapons = new HashSet<>();
}
