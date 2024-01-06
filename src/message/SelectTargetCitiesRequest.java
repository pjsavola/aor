package message;

import aor.Client;
import aor.GameState;
import aor.Node;

import javax.swing.*;
import java.io.Serial;
import java.util.*;

public class SelectTargetCitiesRequest extends Request<SelectTargetCitiesResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<String> options;
    public final int count;
    public final int colonyLimit;
    public final boolean newWorld;
    public boolean reduce;

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count, boolean reduce, int colonyLimit, boolean newWorld) {
        super(info, gameState);
        this.options = new ArrayList<>(options);
        this.count = count;
        this.reduce = reduce;
        this.colonyLimit = colonyLimit;
        this.newWorld = newWorld;
    }

    @Override
    public boolean validateResponse(SelectTargetCitiesResponse response) {
        final long newWorldCount = response.getCities().stream().map(Node::isInNewWorld).count();
        return response.getCities().size() == count &&
                new HashSet<>(options).containsAll(response.getCities()) &&
                response.getCities().stream().map(Node::isInAsia).count() + newWorldCount <= colonyLimit &&
                (newWorldCount == 0 || newWorld);
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
    public boolean clicked(Response pendingResponse, Node node, Client client) {
        final SelectTargetCitiesResponse response = (SelectTargetCitiesResponse) pendingResponse;
        final List<String> cities = response.getCities();
        final long newWorldCount = response.getCities().stream().map(Node::isInNewWorld).count();
        if (cities.size() >= count) return false;
        if (cities.contains(node.getName())) return false;
        if (cities.stream().map(n -> Node.nodeMap.get(n)).filter(Node::isInAsia).count() + newWorldCount >= colonyLimit) return false;
        if (node.isInNewWorld() && !newWorld) return false;

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

    @Override
    public Response reset() {
        return new SelectTargetCitiesResponse();
    }
}
