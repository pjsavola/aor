import java.util.*;

public class Player {

    private static final int maxTokenCount = 36;
    private int cash;
    private int misery;
    private List<Advance> advances = new ArrayList<>();
    private List<Advance> newAdvances = new ArrayList<>();

    private List<Node> cities = new ArrayList<>();
    private List<Node> newCities = new ArrayList<>();

    private Map<Node, Integer> tokens = new HashMap<>();
    private Map<Node, Integer> newTokens = new HashMap<>();

    public int getIncome(int playerCount, int cityCount) {
        final int baseIncome = advances.contains(Advance.middleClass) ? 25 : 15;
        final int income = baseIncome + Math.min(25, cityCount) * playerCount;
        final int interest = advances.contains(Advance.interestAndProfit) ? Math.min(cash, income) : 0;
        return income + interest;
    }

    public List<Advance> getAdvances() {
        return advances;
    }

    public void research(Advance advance, int cost) {
        if (cash >= cost) {
            newAdvances.add(advance);
            cash -= cost;
        }
    }

    public void purchasePhaseFinished() {
        int setCount = getSetCount();
        advances.addAll(newAdvances);
        newAdvances.clear();
        final int newSetCount = getSetCount();
        while (newSetCount > setCount) {
            miseryRelief(setCount);
            ++setCount;
        }
    }

    public void miseryRelief(int amount) {
        misery = Math.max(0, misery - amount);
    }

    private int getSetCount() {
        final int civics = (int) advances.stream().filter(a -> a.category == Advance.Category.CIVICS).count();
        final int commerce = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMERCE).count();
        final int communications = (int) advances.stream().filter(a -> a.category == Advance.Category.COMMUNICATION).count();
        return Math.min(Math.min(civics, commerce), communications);
    }
}
