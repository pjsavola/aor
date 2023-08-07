package message;

import java.io.Serial;

public class IntegerResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int i;

    public IntegerResponse(int i) {
        this.i = i;
    }

    public int getInt() {
        return i;
    }
}
