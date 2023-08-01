package message;

import java.util.Map;

public class ExpansionResponse extends Response {
    private final Map<String, Integer> tokensUsed;
    private final int tokensDisbanded;

    public ExpansionResponse(Map<String, Integer> tokensUsed, int tokensDisbanded) {
        this.tokensUsed = tokensUsed;
        this.tokensDisbanded = tokensDisbanded;
    }

    public Map<String, Integer> getTokensUsed() {
        return tokensUsed;
    }

    public int getTokensDisbanded() {
        return tokensDisbanded;
    }
}
