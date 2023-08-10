package message;

import aor.Commodity;

import java.io.Serial;

public class CommodityResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Commodity commodity;
    private final int adjustment;

    public CommodityResponse(Commodity commodity) {
        this(commodity, 0);
    }

    public CommodityResponse(Commodity commodity, int adjustment) {
        this.commodity = commodity;
        this.adjustment = adjustment;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public int getAdjustment() {
        return adjustment;
    }
}
