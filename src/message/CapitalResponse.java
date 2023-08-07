package message;

import aor.Node;

import java.io.Serial;

public class CapitalResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Node.CityState capital;

    public CapitalResponse(Node.CityState capital) {
        this.capital = capital;
    }

    public Node.CityState getCapital() {
        return capital;
    }
}
