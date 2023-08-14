package aor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public int epoch;
    public int deckSize;
    public int round;
    public Server.Phase phase;
    public List<PlayerState> players = new ArrayList<>();
    public int[] turnOrder;
    public int war1;
    public int war2;
    public List<Commodity> shortages = new ArrayList<>();
    public List<Commodity> surpluses = new ArrayList<>();
    public Advance.Category bannedCategory;
    public int[] playedCards;
    public int[] patronageCards;
    public int[] patronageOwners;
    public int[] patronageUsesRemaining;
}
