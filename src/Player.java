import java.util.*;

public class Player {

    private final Game game = null;
    private static final int maxTokenCount = 36;
    private int cash;
    private int misery;
    private Set<Advance> advances = new HashSet<>();
    private List<Advance> newAdvances = new ArrayList<>();

    List<Node> cities = new ArrayList<>();
    private List<Node> newCities = new ArrayList<>();

    private Map<Node, Integer> tokens = new HashMap<>();
    private Map<Node, Integer> newTokens = new HashMap<>();

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
    }

    private int getSetCount() {
        final int civics = (int) advances.stream().filter(a -> a.category == Advance.Category.CIVICS).count();
        final int commerce = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMERCE).count();
        final int communications = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMUNICATION).count();
        return Math.min(Math.min(civics, commerce), communications);
    }
}
