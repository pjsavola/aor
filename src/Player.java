import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Player {

    private int tokens = 36;
    private int money;
    private int misery;
    private List<Advance> advances = new ArrayList<>();
    private List<Advance> newAdvances = new ArrayList<>();

    public int getIncome(int playerCount, int cityCount) {
        return 15 + Math.min(25, cityCount) * playerCount;
    }

    public List<Advance> getAdvances() {
        return advances;
    }

    public void research(Advance advance, int cost) {
        if (money >= cost) {
            newAdvances.add(advance);
            money -= cost;
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
        final int civics = (int) advances.stream().map(Advance::getCategory).filter(c -> c.equals("Civics")).count();
        final int commerce = (int) advances.stream().map(Advance::getCategory).filter(c -> c.equals("Commerce")).count();
        final int communications = (int) advances.stream().map(Advance::getCategory).filter(c -> c.equals("Communications")).count();
        return Math.min(Math.min(civics, commerce), communications);
    }
}
