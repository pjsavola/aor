package message;

import aor.Advance;
import aor.GameState;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectCategoryRequest extends Request<CategoryResponse> {
    @Serial
    private static final long serialVersionUID = 1L;
    public final List<Advance.Category> options;

    public SelectCategoryRequest(GameState gameState, Set<Advance.Category> options) {
        super("Ban Science, Religion or Exploraion?", gameState);
        this.options = new ArrayList<>(options);
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
