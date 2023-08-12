package message;

import aor.Capital;

import java.io.Serial;

public class CapitalResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Capital capital;

    public CapitalResponse(Capital capital) {
        this.capital = capital;
    }

    public Capital getCapital() {
        return capital;
    }
}
