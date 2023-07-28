public enum Commodity {
    STONE(1),
    WOOL(2),
    TIMBER(3),
    GRAIN(4),
    CLOTH(5),
    WINE(5),
    METAL(6),
    FUR(7),
    SILK(8),
    SPICE(9),
    GOLD(10),
    IVORY(10);

    private final int value;

    private Commodity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String capitalized(Commodity commodity) {
        return commodity.name().charAt(0) + commodity.name().substring(1).toLowerCase();
    }
}
