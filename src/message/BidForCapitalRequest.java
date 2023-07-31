package message;

import aor.Card;

import java.util.Arrays;
import java.util.List;

public class BidForCapitalRequest extends Request<IntegerResponse> {

    public BidForCapitalRequest() {
        super("Bid for Capital");
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() >= 0 && response.getInt() <= 40;
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(0);
    }
}
