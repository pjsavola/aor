package message;

public class IntegerResponse extends Response {
    private final int i;

    public IntegerResponse(int i) {
        this.i = i;
    }

    public int getInt() {
        return i;
    }
}
