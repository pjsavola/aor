package test;

import aor.Card;
import aor.Cards;
import aor.ClientConnection;
import aor.Server;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class TestServer extends Server {

    public Deque<Integer> predefinedRandomNumbers = new ArrayDeque();
    public int predefinedShuffles;

    public TestServer(List<ClientConnection> connections) {
        super(connections);
    }

    @Override
    protected int getRandomNumber(int bound) {
        return predefinedRandomNumbers.isEmpty() ? super.getRandomNumber(bound) : Math.min(bound - 1, predefinedRandomNumbers.pop());
    }

    @Override
    protected void shuffle(List<?> list) {
        if (predefinedRandomNumbers.isEmpty()) {
            if (predefinedShuffles > 0) {
                --predefinedShuffles;
            } else {
                super.shuffle(list);
            }
        }
    }
}
