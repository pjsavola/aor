import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Advance {
    private final String name;
    private final String category;
    private final int cost;
    private final int credits;
    private final Set<Advance> prerequisites;

    public static final Advance[] allAdvances = new Advance[26];

    static {
        allAdvances[0] = new Advance("Heavens", "Science", 30, 20, Collections.emptySet());
        allAdvances[1] = new Advance("Human Body", "Science", 60, 20, Collections.emptySet());
        allAdvances[2] = new Advance("Laws of Matter", "Science", 90,20, Collections.emptySet());
        allAdvances[3] = new Advance("Englightenment", "Science", 120, 20, Collections.emptySet());

        allAdvances[4] = new Advance("Patronage", "Religion", 30, 20, Collections.emptySet());
        allAdvances[5] = new Advance("Holy Indulgence", "Religion", 60, 20, Collections.emptySet());
        allAdvances[6] = new Advance("Proselytism", "Religion", 90, 20, Collections.emptySet());
        allAdvances[7] = new Advance("Cathedral", "Religion", 120, 20, Set.of(allAdvances[5]));

        allAdvances[8] = new Advance("Caravan", "Commerce", 20, 10, Collections.emptySet());
        allAdvances[9] = new Advance("Wind/Watermill", "Commerce", 40, 10, Set.of(allAdvances[8]));
        allAdvances[10] = new Advance("Improved Agriculture", "Commerce", 50, 10, Set.of(allAdvances[9]));
        allAdvances[11] = new Advance("Interest & Profit", "Commerce", 80, 10, Set.of(allAdvances[10]));
        allAdvances[12] = new Advance("Industry", "Commerce", 110, 0, Set.of(allAdvances[11]));

        allAdvances[13] = new Advance("Written Record", "Communication", 30, 30, Collections.emptySet());
        allAdvances[14] = new Advance("Printed Word", "Communication", 60, 20, Set.of(allAdvances[13]));
        allAdvances[15] = new Advance("Master Art", "Communication", 90, 10, Set.of(allAdvances[14]));
        allAdvances[16] = new Advance("Renaissance", "Communication", 120, 0, Set.of(allAdvances[15]));

        final Advance urbanAscendany = new Advance("Urban Ascendancy", "Civics", 20, 20, Collections.emptySet());
        allAdvances[17] = new Advance("Overland East", "Exploration", 40, 20, Collections.emptySet());
        allAdvances[18] = new Advance("Seaworthy Vessels", "Exploration", 80, 20, Collections.emptySet());
        allAdvances[19] = new Advance("Ocean Navigation", "Exploration", 120, 20, Set.of(allAdvances[0], allAdvances[18]));
        allAdvances[20] = new Advance("New World", "Exploration", 160, 20, Set.of(allAdvances[19],urbanAscendany));

        allAdvances[21] = urbanAscendany;
        allAdvances[22] = new Advance("Nationalism", "Civics", 60, 30, Collections.emptySet());
        allAdvances[23] = new Advance("Institutional Research", "Civics", 100, 40, Collections.emptySet());
        allAdvances[24] = new Advance("Cosmopolitan", "Civics", 150, 50, Set.of(allAdvances[17]));
        allAdvances[25] = new Advance("Middle Class", "Civics", 170, 60, Set.of(allAdvances[10]));
    }

    public Advance(String name, String category, int cost, int credits, Set<Advance> prerequisites) {
        this.name = name;
        this.category = category;
        this.cost = cost;
        this.credits = credits;
        this.prerequisites = prerequisites;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void research(Player player) {
        final Set<Advance> advances = new HashSet<>(player.getAdvances());
        if (advances.contains(this)) return;
        if (!advances.containsAll(prerequisites)) return;

        int cost = this.cost;
        if (advances.contains(allAdvances[23]) && !category.equals("Civics") && !category.equals("Religion")) cost -= 10; // Institutional Research
        cost -= advances.stream().filter(a -> a.category.equals(category)).map(a -> a.credits).mapToInt(Integer::intValue).sum();
        if (cost < 0) cost = 0;
        player.research(this, cost);
        if (this == allAdvances[1]) player.miseryRelief(1); // Human Body
    }
}
