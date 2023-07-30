package message;

public class UseAdvancesResponse extends Response {

    public enum RenaissanceMovement { NO_MOVE, MOVE_FRONT, MOVE_BACK }

    private boolean useUrbanAscendancy;
    private RenaissanceMovement useRenaissance = RenaissanceMovement.NO_MOVE;

    public UseAdvancesResponse urbanAscendancy() {
        useUrbanAscendancy = true;
        return this;
    }

    public UseAdvancesResponse renaissance(RenaissanceMovement movement) {
        useRenaissance = movement;
        return this;
    }

    public boolean useUrbanAscendancy() {
        return useUrbanAscendancy;
    }

    public RenaissanceMovement useRenaissance() {
        return useRenaissance;
    }
}
