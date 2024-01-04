package test;

import aor.Advance;
import aor.Capital;
import aor.Card;
import aor.Lobby;
import message.*;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeavensTest {
    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(3);
        final Thread thread = Test.initializeTestWithAdvances(clients, 3, 60, Advance.heavens);
        final ExpansionResponse expansionResponseVenice = new ExpansionResponse(20);
        expansionResponseVenice.addTokens("Belgrade", 1);
        expansionResponseVenice.addTokens("Bari", 2);
        clients.get(0).predefinedResponses.add(expansionResponseVenice);
        final ExpansionResponse expansionResponseGenoa = new ExpansionResponse(20);
        expansionResponseGenoa.addTokens("Granada", 2);
        expansionResponseGenoa.addTokens("Tunis", 2);
        expansionResponseGenoa.addTokens("Valencia", 2);
        clients.get(1).predefinedResponses.add(expansionResponseGenoa);
        final ExpansionResponse expansionResponseBarcelona = new ExpansionResponse(20);
        expansionResponseBarcelona.addTokens("Sicily", 2);
        expansionResponseBarcelona.addTokens("Bari", 1);
        expansionResponseBarcelona.addTokens("Fez", 2);
        expansionResponseBarcelona.addTokens("Algers", 1);
        expansionResponseBarcelona.addTokens("Tunis", 1);
        clients.get(2).predefinedResponses.add(expansionResponseBarcelona);
        thread.start();
    }
}
