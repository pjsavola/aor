package message;

import aor.Advance;

import java.io.Serial;

public class CategoryResponse extends Response {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Advance.Category category;

    public CategoryResponse(Advance.Category category) {
        this.category = category;
    }

    public Advance.Category getCategory() {
        return category;
    }
}
