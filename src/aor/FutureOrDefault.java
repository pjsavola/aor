package aor;

import message.Request;
import message.Response;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class FutureOrDefault<T extends Request<U>, U extends Response> {
    private final CompletableFuture<U> result;
    private final T request;

    public FutureOrDefault(Player player, T request) {
        if (request.gameState != null) {
            request.gameState.current = player.getCapital();
        }
        this.request = request;
        result = player.send(request);
    }

    public U get() {
        try {
            final U response = result.get();
            return request.validateResponse(response) ? response : request.getDefaultResponse();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return request.getDefaultResponse();
        }
    }
}
