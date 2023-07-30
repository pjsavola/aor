package message;

import aor.Card;

import java.util.Arrays;
import java.util.List;

public class SelectCardRequest extends Request {

    private final String info;
    private final int[] indices;

    public SelectCardRequest(String info, List<Card> cards) {
        this.info = info;
        indices = cards.stream().map(Card::getIndex).mapToInt(Integer::intValue).toArray();
    }

    public final List<Card> getCards() {
        return Arrays.stream(indices).mapToObj(i -> Card.allCards.get(i)).toList();
    }
}
