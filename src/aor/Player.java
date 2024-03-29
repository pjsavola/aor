package aor;

import message.Notification;
import message.Request;
import message.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Player {

    public static final int[] miserySteps = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200, 250, 300, 350, 400, 450, 500, 600, 700, 800, 900, 1000 };
    private final Server game;
    public static final int maxTokenCount = 36;
    private int cash;
    int writtenCash;
    int misery;
    private Set<Advance> advances = new HashSet<>();
    private List<Advance> newAdvances = new ArrayList<>();
    List<Card> cards = new ArrayList<>();

    private Capital capital;
    private Map<Node, Integer> areas = new HashMap<>();
    private Map<Node, Integer> newAreas = new HashMap<>();
    private int remainingTokens = maxTokenCount;
    public Set<Integer> weapons = new HashSet<>();
    private int usableTokens;
    public int shipLevel;
    int renaissanceUsed;
    final int[] cathedralUsed;
    boolean chaos;
    private final ClientConnection client;

    public Player(Server game, ClientConnection client, int playerCount) {
        this.game = game;
        this.client = client;
        cathedralUsed = new int[playerCount];
        cash = game.getInitialCash();
    }

    public int getCash() {
        return cash;
    }

    public int getUsableTokens() {
        return usableTokens;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public void moveTokens(int amount) {
        usableTokens += amount;
        remainingTokens -= amount;
        System.err.println("Usable tokens: " + usableTokens);
    }

    public void spendTokens(int amount) {
        usableTokens -= amount;
        System.err.println("Usable tokens: " + usableTokens);
    }

    public void addCity(Node node) {
        areas.put(node, node.getSize());
    }

    public Stream<Node> getAreas() {
        return areas.keySet().stream();
    }

    public int getTokenCount(Node area) {
        return areas.getOrDefault(area, 0) + newAreas.getOrDefault(area, 0);
    }

    public void addNewTokens(Node area, int count) {
        newAreas.put(area, getTokenCount(area) + count);
    }

    public void addNewCity(Node area) {
        newAreas.put(area, area.getSize());
    }

    public void removeAllTokens(Node area) {
        final int old = getTokenCount(area);
        areas.remove(area);
        newAreas.remove(area);
        if (old < area.getSize() || area.getSize() == 1) {
            remainingTokens += old;
        }
    }

    public Stream<Node> getTokenAreas() {
        return areas.entrySet().stream().filter(e -> e.getKey().getSize() > e.getValue() || e.getValue() == 1).map(Map.Entry::getKey);
    }

    public Stream<Node> getFullAreas() {
        return areas.entrySet().stream().filter(e -> e.getKey().getSize() == e.getValue()).map(Map.Entry::getKey);
    }

    public Stream<Node> getCities() {
        return areas.entrySet().stream().filter(e -> e.getKey().getSize() == e.getValue() && e.getValue() > 1).map(Map.Entry::getKey);
    }

    public void reduceCity(Node node) {
        System.err.println("Reduced " + node.getName());
        final Integer tokens = areas.get(node);
        if (tokens != null && tokens == node.getSize()) {
            if (remainingTokens > 0) {
                --remainingTokens;
                areas.put(node, 1);
            } else if (usableTokens > 0) {
                --usableTokens;
                areas.put(node, 1);
            } else {
                areas.put(node, 0);
            }
        }
    }

    public int getNewCityCount() {
        return (int) newAreas.entrySet().stream().filter(e -> e.getKey().getSize() == e.getValue() && e.getValue() > 1).count();
    }

    public PlayerState getState() {
        final PlayerState state = new PlayerState();
        state.capital = capital;
        state.numberOfCards = cards.size();
        state.cash = cash;
        state.writtenCash = writtenCash;
        state.misery = misery;
        state.chaos = chaos;
        state.cathedralUsed = Arrays.copyOf(cathedralUsed, cathedralUsed.length);
        state.advances = advances.stream().map(Advance::getIndex).mapToInt(Integer::intValue).toArray();
        state.shipLevel = shipLevel;
        state.usableTokens = usableTokens;
        state.remainingTokens = remainingTokens;
        areas.forEach((key, value) -> {
            state.areas.add(key.getName());
            state.tokens.add(value);
        });
        newAreas.forEach((key, value) -> {
            state.newAreas.add(key.getName());
            state.newTokens.add(value);
        });
        state.weapons.addAll(weapons);
        return state;
    }

    public Capital getCapital() {
        return capital;
    }

    public <T extends Request<U>, U extends Response> CompletableFuture<U> send(T request) {
        return CompletableFuture.supplyAsync(() -> client.request(request));
    }

    public <T extends Notification> void notify(T notification) {
        CompletableFuture.runAsync(() -> client.notify(notification)).join();
    }

    public <T extends Notification> void notifyOthers(T notification) {
        game.players.stream().filter(p -> p != this).forEach(p -> notify(notification));
    }

    public void selectCapital(Capital capital) {
        this.capital = capital;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addTokens(int bid) {
        final int tokens = Math.max(0, Math.min(bid, remainingTokens));
        moveTokens(tokens);
    }

    public void flipTokens() {
        newAreas.forEach((node, count) -> areas.merge(node, count, Integer::sum));
        newAreas.clear();
    }

    public int getIncome(int playerCount) {
        final int baseIncome = advances.contains(Advance.middleClass) ? 25 : 15;
        final int income = baseIncome + (int) Math.min(25, getCities().count()) * playerCount;
        final int interest = advances.contains(Advance.interestAndProfit) ? Math.min(cash, income) : 0;
        return income + interest;
    }

    public int getCommodityCount(Commodity commodity) {
        return (int) getCities().filter(c -> c.hasCommodity(commodity)).count();
    }

    public Set<Advance> getAdvances() {
        return advances;
    }

    public Set<Advance> getAllAdvances() {
        final Set<Advance> allAdvances = new HashSet<>(advances);
        allAdvances.addAll(newAdvances);
        return allAdvances;
    }

    public void research(Advance advance) {
        final Set<Advance> allAdvances = getAllAdvances();
        if (!allAdvances.containsAll(advance.prerequisites)) return;

        final int cost = advance.getCost(advances);
        final LeaderCard card = game.getBestLeaderCard(advance, this, allAdvances);
        final int discount = card == null ? 0 : card.getAmount(game.playedCards);
        final int finalCost = Math.max(0, cost - discount);
        if (cash >= finalCost) {
            game.log(this + " developed " + advance + " for " + finalCost);
            if (cost > 0 && discount > 0) {
                card.use(this);
                game.log(this + " used " + card.getName());
            }
            newAdvances.add(advance);
            cash -= finalCost;
            if (advance == Advance.humanBody || advance == Advance.improvedAgriculture) {
                adjustMisery(-1);
            }
            if (advance.category == Advance.Category.RELIGION) {
                adjustMisery(1);
            }
            if (advance == Advance.seaworthyVessels || advance == Advance.oceanNavigation) {
                shipLevel = 1;
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
                    final int rebate = card.getAmount(game.playedCards);
                    game.log(this + " gains " + rebate + " rebate");
                    cash += rebate;
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
            game.log(this + " goes to chaos");
            System.err.println(this + " goes to chaos, " + cards.size() + " cards discarded");
            chaos = true;
            cards.forEach(game::moveToNextDeck);
            cards.clear();
        } else {
            if (delta > 0) game.log(this + " gains " + delta + " misery");
            else if (delta < 0) game.log(this + " reliefs " + delta + " misery");
        }
    }

    public void remove(Node node) {
        final Integer tokens = areas.remove(node);
        if (tokens != null) {
            if (tokens < node.getSize()) {
                remainingTokens += tokens;
            }
        }
        final Integer newTokens = newAreas.remove(node);
        if (newTokens != null) {
            if (newTokens < node.getSize()) {
                remainingTokens += newTokens;
            }
        }
    }

    private int getSetCount() {
        final int civics = (int) advances.stream().filter(a -> a.category == Advance.Category.CIVICS).count();
        final int commerce = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMERCE).count();
        final int communications = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMUNICATION).count();
        return Math.min(Math.min(civics, commerce), communications);
    }

    @Override
    public String toString() {
        return capital == null ? ("Player " + (game.players.indexOf(this) + 1)) : capital.name();
    }
}
