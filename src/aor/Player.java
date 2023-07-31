package aor;

import message.Notification;
import message.Request;
import message.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Player {

    public static final int[] miserySteps = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200, 250, 300, 350, 400, 450, 500, 600, 700, 800, 900, 1000 };
    private final Game game;
    private static final int maxTokenCount = 36;
    private int cash = 40;
    int writtenCash;
    int misery;
    private Set<Advance> advances = new HashSet<>();
    private List<Advance> newAdvances = new ArrayList<>();
    List<Card> cards = new ArrayList<>();

    private Node.CityState capital;
    List<Node> cities = new ArrayList<>();
    private List<Node> newCities = new ArrayList<>();

    private Map<Node, Integer> tokens = new HashMap<>();
    private Map<Node, Integer> newTokens = new HashMap<>();
    public int usableTokens;
    int renaissanceUsed;
    boolean chaos;
    private Client client = new Client(null);

    public Player(Game game) {
        this.game = game;
    }

    public int getCash() {
        return cash;
    }

    public PlayerState getState() {
        final PlayerState state = new PlayerState();
        state.capital = capital;
        state.numberOfCards = cards.size();
        state.cash = cash;
        state.writtenCash = writtenCash;
        return state;
    }

    public Node.CityState getCapital() {
        return capital;
    }

    public <T extends Request, U extends Response> CompletableFuture<U> send(T request) {
        return CompletableFuture.supplyAsync(() -> client.request(request));
    }

    public <T extends Notification> void notify(T notification) {
        CompletableFuture.runAsync(() -> client.notify(notification));
    }

    public void selectCapital(Node.CityState capital) {
        this.capital = capital;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addTokens(int bid) {
        final int maxTokens = getRemainingTokens();
        usableTokens = Math.max(0, Math.min(bid, maxTokens));
    }

    public int getRemainingTokens() {
        final int usedTokens = tokens.values().stream().mapToInt(Integer::intValue).sum();
        return maxTokenCount - usedTokens - usableTokens;
    }

    public int getIncome(int playerCount) {
        cities.addAll(newCities);
        newCities.clear();
        tokens.putAll(newTokens);
        newTokens.clear();
        final int baseIncome = advances.contains(Advance.middleClass) ? 25 : 15;
        final int income = baseIncome + Math.min(25, cities.size()) * playerCount;
        final int interest = advances.contains(Advance.interestAndProfit) ? Math.min(cash, income) : 0;
        return income + interest;
    }

    public int getCommodityCount(Commodity commodity) {
        return (int) cities.stream().filter(c -> c.hasCommodity(commodity)).count();
    }

    public Set<Advance> getAdvances() {
        return advances;
    }

    public void research(Advance advance) {
        final Set<Advance> allAdvances = new HashSet<>(advances);
        allAdvances.addAll(newAdvances);
        if (!allAdvances.containsAll(advance.prerequisites)) return;

        final int cost = advance.getCost(game, this);
        final LeaderCard card = game.getBestLeaderCard(advance, this);
        final int discount = card == null ? 0 : card.amount;
        final int finalCost = Math.max(0, cost - discount);
        if (cash >= finalCost) {
            if (cost > 0 && discount > 0) {
                card.use(this);
            }
            newAdvances.add(advance);
            cash -= finalCost;
            if (advance == Advance.humanBody || advance == Advance.improvedAgriculture) {
                adjustMisery(-1);
            }
            if (advance.category == Advance.Category.RELIGION) {
                adjustMisery(1);
            }
        }
    }

    public void purchasePhaseFinished() {
        int setCount = getSetCount();
        advances.addAll(newAdvances);
        newAdvances.clear();
        final int newSetCount = getSetCount();
        while (newSetCount > setCount) {
            adjustMisery(-setCount);
            ++setCount;
        }
    }

    public void gainRebate(LeaderCard card) {
        if (advances.contains(Advance.printedWord)) {
            for (Advance advance : card.advances) {
                if (advances.contains(advance)) {
                    cash += card.getAmount(game);
                    break;
                }
            }
        }
    }

    public void adjustCash(int delta) {
        cash += delta;
    }

    public void adjustMisery(int delta) {
        if (chaos) return;

        misery = Math.max(0, misery + delta);
        if (misery >= miserySteps.length) {
            chaos = true;
            cards.forEach(game::moveToNextDeck);
            cards.clear();
        }
    }

    public void reduce(List<Node> nodes) {
        nodes.forEach(tokens::remove);
        nodes.forEach(this::reduce);
    }

    public void reduce(Node node) {
        if (cities.remove(node)) {
            if (getRemainingTokens() == 0) {
                if (usableTokens <= 0) return;
                --usableTokens;
            }
            tokens.put(node, 1);
        } else {
            tokens.remove(node);
        }
    }

    private int getSetCount() {
        final int civics = (int) advances.stream().filter(a -> a.category == Advance.Category.CIVICS).count();
        final int commerce = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMERCE).count();
        final int communications = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMUNICATION).count();
        return Math.min(Math.min(civics, commerce), communications);
    }
}
