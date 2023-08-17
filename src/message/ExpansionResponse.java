package message;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ExpansionResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<String> tokensUsedKeys;
    private final List<Integer> tokensUsedValues;
    private String cathedralUsed;
    private int tokensDisbanded;
    private boolean cardPurchased;

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

    public ExpansionResponse(int tokens) {
        tokensUsedKeys = new ArrayList<>();
        tokensUsedValues = new ArrayList<>();
        tokensDisbanded = tokens;
    }

    public void addTokens(String area, int amount) {
        final int idx = tokensUsedKeys.indexOf(area);
        if (idx != -1) {
            tokensUsedValues.set(idx, tokensUsedValues.get(idx) + amount);
        } else {
            tokensUsedKeys.add(area);
            tokensUsedValues.add(amount);
        }
        tokensDisbanded -= amount;
    }

    public void removeTokens(String area, int amount) {
        final int idx = tokensUsedKeys.indexOf(area);
        if (idx != -1) {
            final int newValue = tokensUsedValues.get(idx) - amount;
            if (newValue <= 0) {
                tokensUsedKeys.remove(idx);
                tokensUsedValues.remove(idx);
            } else {
                tokensUsedValues.set(idx, newValue);
            }
        }
        tokensDisbanded += amount;
    }

    public int getTokens(String area) {
        final int idx = tokensUsedKeys.indexOf(area);
        if (idx != -1) {
            return tokensUsedValues.get(idx);
        }
        return 0;
    }

    public int getTokenCount() {
        return tokensUsedValues.stream().mapToInt(Integer::intValue).sum();
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

    public boolean isCardPurchased() {
        return cardPurchased;
    }

    public void clearDisbandedTokens() {
        tokensDisbanded = 0;
    }
}
