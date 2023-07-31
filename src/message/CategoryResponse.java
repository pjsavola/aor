package message;

import aor.Advance;

public class CategoryResponse extends Response {
    private final Advance.Category category;

    public CategoryResponse(Advance.Category category) {
        this.category = category;
    }

    public Advance.Category getCategory() {
        return category;
    }
}
