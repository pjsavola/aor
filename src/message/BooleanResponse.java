package message;

public class BooleanResponse extends Response {
    private final boolean b;

    public BooleanResponse(boolean b) {
        this.b = b;
    }

    public boolean getBool() {
        return b;
    }
}
