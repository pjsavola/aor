package message;

import aor.Card;

import java.util.Arrays;
import java.util.List;

public class SelectDiscardRequest extends Request {
    private final int[] indices;

    public SelectDiscardRequest(List<Card> cards) {
        indices = cards.stream().map(Card::getIndex).mapToInt(Integer::intValue).toArray();
    }

    public final List<Card> getCards() {
        return Arrays.stream(indices).mapToObj(i -> Card.allCards.get(i)).toList();
    }
}
