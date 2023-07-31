package aor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Node.CityState capital;
    public int numberOfCards;
    public int cash;
    public int writtenCash;
    public int misery;
    public int[] advances;
    public List<Integer> ownedPatronageCards = new ArrayList<>();
}
