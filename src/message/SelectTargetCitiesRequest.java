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
    public final int count;
    public final int asiaLimit;
    public final int newWorldLimit;
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

    @Override
    public boolean clicked(Response pendingResponse, Node node) {
        final SelectTargetCitiesResponse response = (SelectTargetCitiesResponse) pendingResponse;
        final List<String> cities = response.getCities();
        if (cities.size() >= count) return false;
        if (cities.contains(node.getName())) return false;
        if (cities.stream().map(n -> Node.nodeMap.get(n)).filter(Node::isInAsia).count() >= asiaLimit) return false;
        if (cities.stream().map(n -> Node.nodeMap.get(n)).filter(Node::isInNewWorld).count() >= newWorldLimit) return false;

        if (options.contains(node.getName())) {
            response.addCity(node.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean highlight(Response response, Node node) {
        return options.stream().anyMatch(n -> n.equals(node.getName()));
    }
}
