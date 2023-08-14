package message;

import java.io.Serial;
import java.io.Serializable;

public class LogEntryNotification extends Notification {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String text;

    public LogEntryNotification(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
