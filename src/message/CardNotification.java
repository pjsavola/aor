package message;

import aor.Card;

import java.io.Serial;

public class CardNotification extends Notification {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int index;

    public CardNotification(Card card) {
        index = card.getIndex();
    }

    public Card getCard() {
        return Card.allCards.get(index);
    }
}
