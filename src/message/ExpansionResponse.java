package message;

public class ExpansionResponse extends Response {
    private final int i;

    public ExpansionResponse(int i) {
        this.i = i;
    }

    public int getInt() {
        return i;
    }
}
