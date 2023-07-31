package message;

import aor.Advance;
import aor.GameState;

import java.util.HashSet;
import java.util.Set;

public class SelectCategoryRequest extends Request<CategoryResponse> {
    public final Set<Advance.Category> options;

    public SelectCategoryRequest(GameState gameState, Set<Advance.Category> options) {
        super("Ban Science, Religion or Exploraion?", gameState);
        this.options = options;
    }

    @Override
    public boolean validateResponse(CategoryResponse response) {
        return response.getCategory() == null || options.contains(response.getCategory());
    }

    @Override
    public CategoryResponse getDefaultResponse() {
        return new CategoryResponse(null);
    }
}
