package message;

import aor.Client;
import aor.GameState;
import aor.Node;

import java.io.Serial;
import java.util.*;

public class SelectTargetCitiesRequest extends Request<SelectTargetCitiesResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<String> options;
    public int count;
    public int asiaLimit;
    public int newWorldLimit;
    public boolean reduce;

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count, boolean reduce) {
        this(info, gameState, options, count, reduce, 3, 2);
    }

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count, boolean reduce, int asiaLimit, int newWorldLimit) {
        super(info, gameState);
        this.options = new ArrayList<>(options);
        this.count = count;
        this.reduce = reduce;
        this.asiaLimit = asiaLimit;
        this.newWorldLimit = newWorldLimit;
    }

    @Override
    public boolean validateResponse(SelectTargetCitiesResponse response) {
        return response.getCities().size() == count &&
                new HashSet<>(options).containsAll(response.getCities()) &&
                response.getCities().stream().map(Node::isInAsia).count() <= asiaLimit &&
                response.getCities().stream().map(Node::isInNewWorld).count() <= newWorldLimit;
    }

    @Override
    public SelectTargetCitiesResponse getDefaultResponse() {
        return new SelectTargetCitiesResponse(new String[] { options.iterator().next() });
    }

    @Override
    public void handleRequest(Client client) {
        client.handleRequest(this);
    }
}
