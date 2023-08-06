package message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ExpansionResponse extends Response {
    private final List<String> tokensUsedKeys;
    private final List<Integer> tokensUsedValues;
    private final String cathedralUsed;
    private final int tokensDisbanded;

    public ExpansionResponse(Map<String, Integer> tokensUsed, int tokensDisbanded, String cathedralUsed) {
        tokensUsedKeys = new ArrayList<>(tokensUsed.size());
        tokensUsedValues = new ArrayList<>(tokensUsed.size());
        tokensUsed.forEach((key, value) -> {
            tokensUsedKeys.add(key);
            tokensUsedValues.add(value);
        });
        this.tokensDisbanded = tokensDisbanded;
        this.cathedralUsed = cathedralUsed;
    }

    public Stream<Map.Entry<String, Integer>> getEntryStream() {
        return tokensUsedKeys.stream().map(key -> Map.entry(key, tokensUsedValues.get(tokensUsedKeys.indexOf(key))));
    }

    public int getTokensUsed() {
        return tokensUsedValues.stream().mapToInt(Integer::intValue).sum();
    }

    public String getCathedralused() {
        return cathedralUsed;
    }

    public int getTokensDisbanded() {
        return tokensDisbanded;
    }
}
