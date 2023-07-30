package message;

import aor.Node;

public class CapitalResponse extends Response {
    private final Node.CityState capital;

    public CapitalResponse(Node.CityState capital) {
        this.capital = capital;
    }

    public Node.CityState getCapital() {
        return capital;
    }
}
