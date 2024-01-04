package test;

import aor.Advance;
import aor.Capital;
import aor.Card;
import aor.Lobby;
import message.BooleanResponse;
import message.CapitalResponse;
import message.IntegerResponse;
import message.PurchaseAdvancesResponse;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HeavensTest {
    public static void main(String[] args) {
        final List<TestClient> clients = new ArrayList<>(3);
        final Thread thread = Test.initializeTestWithAdvances(clients, 3, 60, Advance.heavens);
        thread.start();
    }
}
