package aor;

import java.awt.*;

public enum Capital {
    VENICE(new Color(0xDDAA44)),
    GENOA(new Color(0xCC66DD)),
    BARCELONA(new Color(0x7799DD)),
    PARIS(new Color(0xDD6655)),
    LONDON(new Color(0x55BB55)),
    HAMBURG(new Color(0xBB7766));

    private final Color color;
    private Capital(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
