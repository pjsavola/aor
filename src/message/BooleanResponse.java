package message;

import java.io.Serial;

public class BooleanResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean b;

    public BooleanResponse(boolean b) {
        this.b = b;
    }

    public boolean getBool() {
        return b;
    }
}
