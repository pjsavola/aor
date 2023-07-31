package message;

import aor.Advance;

import java.util.Arrays;
import java.util.List;

public class PurchaseAdvancesResponse extends Response {
    private final int[] indices;

    public PurchaseAdvancesResponse(List<Advance> advances) {
        indices = advances.stream().map(Advance::getIndex).mapToInt(Integer::intValue).toArray();
    }

    public List<Advance> getAdvances() {
        return Arrays.stream(indices).mapToObj(i -> Advance.allAdvances.get(i)).toList();
    }
}
