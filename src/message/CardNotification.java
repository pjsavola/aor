package message;

import aor.Card;

public class CardNotification extends Notification {
    private final int index;

    public CardNotification(Card card) {
        index = card.getIndex();
    }

    public Card getCard() {
        return Card.allCards.get(index);
    }
}
