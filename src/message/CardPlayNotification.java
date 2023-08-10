package message;

import aor.Card;

import java.io.Serial;

public class CardPlayNotification extends Notification {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int index;
    private final int playerIndex;

    public CardPlayNotification(Card card, int playerIndex) {
        index = card.getIndex();
        this.playerIndex = playerIndex;
    }

    public Card getCard() {
        return Card.allCards.get(index);
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
}
