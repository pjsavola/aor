package aor;

import java.awt.*;

public enum Capital {
    VENICE(new Color(0xDDAA44), 7),
    GENOA(new Color(0xCC66DD), 7),
    BARCELONA(new Color(0x7799DD), 4),
    PARIS(new Color(0xDD6655), 3),
    LONDON(new Color(0x55BB55), 2),
    HAMBURG(new Color(0xBB7766), 1);

    private final Color color;
    private final int region;
    private Capital(Color color, int region) {
        this.color = color;
        this.region = region;
    }

    public Color getColor() {
        return color;
    }

    public int getRegion() {
        return region;
    }
}
