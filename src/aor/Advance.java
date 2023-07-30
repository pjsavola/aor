package aor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Advance {
    final String name;
    final Category category;
    final int cost;
    final int credits;
    final Set<Advance> prerequisites;

    public enum Category { SCIENCE, RELIGION, COMMERCE, COMMUNICATION, EXPLORATION, CIVICS };

    public static final Advance heavens = new Advance("Heavens", Category.SCIENCE, 30, 20, Collections.emptySet());
    public static final Advance humanBody = new Advance("Human Body", Category.SCIENCE, 60, 20, Collections.emptySet());
    public static final Advance lawsOfMatter = new Advance("Laws of Matter", Category.SCIENCE, 90,20, Collections.emptySet());
    public static final Advance enlightenment = new Advance("Englightenment", Category.SCIENCE, 120, 20, Collections.emptySet());

    public static final Advance patronage = new Advance("Patronage", Category.RELIGION, 30, 20, Collections.emptySet());
    public static final Advance holyIndulgence = new Advance("Holy Indulgence", Category.RELIGION, 60, 20, Collections.emptySet());
    public static final Advance proselytism = new Advance("Proselytism", Category.RELIGION, 90, 20, Collections.emptySet());
    public static final Advance cathedral = new Advance("Cathedral", Category.RELIGION, 120, 20, Set.of(holyIndulgence));

    public static final Advance caravan = new Advance("Caravan", Category.COMMERCE, 20, 10, Collections.emptySet());
    public static final Advance windWaterMill = new Advance("Wind/Watermill", Category.COMMERCE, 40, 10, Set.of(caravan));
    public static final Advance improvedAgriculture = new Advance("Improved Agriculture", Category.COMMERCE, 50, 10, Set.of(windWaterMill));
    public static final Advance interestAndProfit = new Advance("Interest & Profit", Category.COMMERCE, 80, 10, Set.of(improvedAgriculture));
    public static final Advance industry = new Advance("Industry", Category.COMMERCE, 110, 0, Set.of(interestAndProfit));

    public static final Advance writtenRecord = new Advance("Written Record", Category.COMMUNICATION, 30, 30, Collections.emptySet());
    public static final Advance printedWord = new Advance("Printed Word", Category.COMMUNICATION, 60, 20, Set.of(writtenRecord));
    public static final Advance masterArt = new Advance("Master Art", Category.COMMUNICATION, 90, 10, Set.of(printedWord));
    public static final Advance renaissance = new Advance("Renaissance", Category.COMMUNICATION, 120, 0, Set.of(masterArt));

    public static final Advance urbanAscendancy = new Advance("Urban Ascendancy", Category.CIVICS, 20, 20, Collections.emptySet());
    public static final Advance overlandEast = new Advance("Overland East", Category.EXPLORATION, 40, 20, Collections.emptySet());
    public static final Advance seaworthyVessels = new Advance("Seaworthy Vessels", Category.EXPLORATION, 80, 20, Collections.emptySet());
    public static final Advance oceanNavigation = new Advance("Ocean Navigation", Category.EXPLORATION, 120, 20, Set.of(heavens, seaworthyVessels));
    public static final Advance newWorld = new Advance("New World", Category.EXPLORATION, 160, 20, Set.of(oceanNavigation,urbanAscendancy));

    public static final Advance nationalism = new Advance("Nationalism", Category.CIVICS, 60, 30, Collections.emptySet());
    public static final Advance institutionalResearch = new Advance("Institutional Research", Category.CIVICS, 100, 40, Collections.emptySet());
    public static final Advance cosmopolitan = new Advance("Cosmopolitan", Category.CIVICS, 150, 50, Set.of(overlandEast));
    public static final Advance middleClass = new Advance("Middle Class", Category.CIVICS, 170, 60, Set.of(improvedAgriculture));

    public Advance(String name, Category category, int cost, int credits, Set<Advance> prerequisites) {
        this.name = name;
        this.category = category;
        this.cost = cost;
        this.credits = credits;
        this.prerequisites = prerequisites;
    }

    public int getCost(Game game, Player player) {
        final Set<Advance> advances = player.getAdvances();
        if (advances.contains(this)) return Integer.MAX_VALUE;
        if (!advances.containsAll(prerequisites)) return Integer.MAX_VALUE;

        int cost = this.cost;
        if (advances.contains(institutionalResearch) && category != Category.CIVICS && category != Category.RELIGION) cost -= 10;
        cost -= Math.min(cost, advances.stream().filter(a -> a.category == category).map(a -> a.credits).mapToInt(Integer::intValue).sum());
        return cost;
    }
}
