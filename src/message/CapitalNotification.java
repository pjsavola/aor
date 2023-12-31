package message;

import aor.Capital;

import java.io.Serial;

public class CapitalNotification extends Notification {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Capital capital;

    public CapitalNotification(Capital capital) {
        this.capital = capital;
    }

    public Capital getCapital() {
        return capital;
    }
}
