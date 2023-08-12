package message;

import aor.Card;
import aor.Client;
import aor.GameState;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectCardRequest extends Request<IntegerResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int[] indices;
    public final boolean optional;

    public SelectCardRequest(String info, GameState gameState, List<? extends Card> cards, boolean optional) {
        super(info, gameState);
        indices = cards.stream().map(Card::getIndex).mapToInt(Integer::intValue).toArray();
        this.optional = optional;
    }

    public final List<Card> getCards() {
        return Arrays.stream(indices).mapToObj(i -> Card.allCards.get(i)).toList();
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() >= (optional ? -1 : 0) && response.getInt() < indices.length;
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(optional ? -1 : 0);
    }

    @Override
    public String toString() {
        return Arrays.stream(indices).mapToObj(Integer::toString).collect(Collectors.joining(" "));
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
