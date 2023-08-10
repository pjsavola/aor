package message;

import aor.Card;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BidForCapitalRequest extends Request<IntegerResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public BidForCapitalRequest() {
        super("Bid for Capital");
    }

    @Override
    public boolean validateResponse(IntegerResponse response) {
        return response.getInt() >= 0 && response.getInt() <= 40;
    }

    @Override
    public IntegerResponse getDefaultResponse() {
        return new IntegerResponse(new Random().nextInt(6));
    }
}
