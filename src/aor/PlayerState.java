package aor;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Node.CityState capital;
    public int numberOfCards;
    public int cash;
    public int writtenCash;
}
