package aor;

import message.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

class FutureOrDefault<T extends Response> {
    private final CompletableFuture<T> result;
    private final T fallback;
    private final Function<T, Boolean> requirement;

    public FutureOrDefault(CompletableFuture<T> result, Function<T, Boolean> requirement, T fallback) {
        this.result = result;
        this.requirement = requirement;
        this.fallback = fallback;
    }

    public T getResult() {
        final T result = get();
        return requirement.apply(result) ? result : fallback;
    }

    private T get() {
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return fallback;
        }
    }
}
