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
    public List<String> areas = new ArrayList<>();
    public List<Integer> tokens = new ArrayList<>();
    public List<String> newAreas = new ArrayList<>();
    public List<Integer> newTokens = new ArrayList<>();
    public List<Integer> weapons = new ArrayList<>();
}
