package message;

import aor.GameState;

import java.util.List;
import java.util.Set;

public class SelectTargetCitiesRequest extends Request<SelectTargetCitiesResponse> {
    public final Set<String> options;
    public final int count;

    public SelectTargetCitiesRequest(String info, GameState gameState, Set<String> options, int count) {
        super(info, gameState);
        this.options = options;
        this.count = count;
    }

    @Override
    public boolean validateResponse(SelectTargetCitiesResponse response) {
        return response.getCities().length == count && options.containsAll(List.of(response.getCities()));
    }

    @Override
    public SelectTargetCitiesResponse getDefaultResponse() {
        return new SelectTargetCitiesResponse(new String[] { options.iterator().next() });
    }
}
