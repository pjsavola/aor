import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Player {

    public static final int[] miserySteps = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200, 250, 300, 350, 400, 450, 500, 600, 700, 800, 900, 1000 };
    private final Game game;
    private static final int maxTokenCount = 36;
    private int cash = 40;
    private int misery;
    private Set<Advance> advances = new HashSet<>();
    private List<Advance> newAdvances = new ArrayList<>();
    private List<Card> cards = new ArrayList<>();

    private Node.CityState capital;
    List<Node> cities = new ArrayList<>();
    private List<Node> newCities = new ArrayList<>();

    private Map<Node, Integer> tokens = new HashMap<>();
    private Map<Node, Integer> newTokens = new HashMap<>();

    public Player(Game game) {
        this.game = game;
    }

    public PlayerState getState() {
        final PlayerState state = new PlayerState();
        state.capital = capital;
        state.numberOfCards = cards.size();
        state.cash = cash;
        return state;
    }

    public <T> CompletableFuture<T> getInput(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public Card discardOne(Card c1, Card c2, Card c3) {
        cards.add(c2);
        cards.add(c3);
        return c1;
    }

    public int bidForCapital() {
        return 0;
    }

    public Node.CityState selectCapital(GameState gameState) {
        capital = Node.CityState.VENICE;
        return Node.CityState.VENICE;
    }

    public int getIncome(int playerCount, int cityCount) {
        final int baseIncome = advances.contains(Advance.middleClass) ? 25 : 15;
        final int income = baseIncome + Math.min(25, cityCount) * playerCount;
        final int interest = advances.contains(Advance.interestAndProfit) ? Math.min(cash, income) : 0;
        return income + interest;
    }

    public Set<Advance> getAdvances() {
        return advances;
    }

    public void research(Advance advance) {
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
        misery = Math.max(0, misery + delta);
        if (misery >= miserySteps.length) {
            // Player drops to chaos...
        }
    }

    private int getSetCount() {
        final int civics = (int) advances.stream().filter(a -> a.category == Advance.Category.CIVICS).count();
        final int commerce = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMERCE).count();
        final int communications = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMUNICATION).count();
        return Math.min(Math.min(civics, commerce), communications);
    }
}
