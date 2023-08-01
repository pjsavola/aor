package message;

import aor.GameState;
import aor.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SelectTargetCitiesRequest extends Request<SelectTargetCitiesResponse> {
    public final Set<String> options;
    public final int count;
    public final int asiaLimit;
    public final int newWorldLimit;

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count) {
        this(info, gameState, options, count, 3, 2);
    }

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count, int asiaLimit, int newWorldLimit) {
        super(info, gameState);
        this.options = options;
        this.count = count;
        this.asiaLimit = asiaLimit;
        this.newWorldLimit = newWorldLimit;
    }

    @Override
    public boolean validateResponse(SelectTargetCitiesResponse response) {
        return response.getCities().length == count &&
                options.containsAll(List.of(response.getCities())) &&
                Arrays.stream(response.getCities()).map(Node::isInAsia).count() <= asiaLimit &&
                Arrays.stream(response.getCities()).map(Node::isInNewWorld).count() <= newWorldLimit;
    }

    @Override
    public SelectTargetCitiesResponse getDefaultResponse() {
        return new SelectTargetCitiesResponse(new String[] { options.iterator().next() });
    }
}
