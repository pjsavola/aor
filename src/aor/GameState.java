package aor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int epoch;
    public int deckSize;
    public int round;
    public Game.Phase phase;
    public List<PlayerState> turnOrder = new ArrayList<>();
    public List<Commodity> shortages = new ArrayList<>();
    public List<Commodity> surpluses = new ArrayList<>();
    public Advance.Category bannedCategory;
    public int[] playedCards;
    public int[] patronageCards;
    public int[] patronageUsesRemaining;
}
