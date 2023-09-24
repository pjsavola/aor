package message;

import aor.Advance;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PurchaseAdvancesResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<Integer> indices;

    public PurchaseAdvancesResponse() {
        indices = new ArrayList<>();
    }

    public PurchaseAdvancesResponse(List<Advance> advances) {
        indices = advances.stream().map(Advance::getIndex).toList();
    }

    public void addAdvance(Advance advance) {
        indices.add(Advance.allAdvances.indexOf(advance));
    }

    public List<Advance> getAdvances() {
        return indices.stream().map(i -> Advance.allAdvances.get(i)).toList();
    }
}
