package aor;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

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
    public List<Integer> ownedPatronageCards = new ArrayList<>();
    public List<String> areas = new ArrayList<>();
    public List<Integer> tokens = new ArrayList<>();
    public List<String> newAreas = new ArrayList<>();
    public List<Integer> newTokens = new ArrayList<>();
    public List<Integer> weapons = new ArrayList<>();
}
