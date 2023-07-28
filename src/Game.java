import java.util.*;

public class Game {

    final List<LeaderCard> patronageQueue = new ArrayList<>();

    private final Deque<Card> phase1 = new ArrayDeque<>();
    private final Deque<Card> phase2 = new ArrayDeque<>();
    private final Deque<Card> phase3 = new ArrayDeque<>();

    private final Set<Card> removedCards = new HashSet<>();

    public Game() {
        phase1.add(new CommodityCard(Commodity.STONE));
        phase1.add(new CommodityCard(Commodity.STONE));
        phase1.add(new CommodityCard(Commodity.WOOL));
        phase1.add(new CommodityCard(Commodity.WOOL));
        phase1.add(new CommodityCard(Commodity.TIMBER));
        phase1.add(new CommodityCard(Commodity.TIMBER));
        phase1.add(new DoubleCommodityCard(Commodity.CLOTH, Commodity.WINE));
        phase1.add(new CommodityCard(Commodity.METAL));
        phase1.add(new CommodityCard(Commodity.FUR));
        phase1.add(new CommodityCard(Commodity.SILK));
        phase1.add(new CommodityCard(Commodity.SPICE));
        phase1.add(new DoubleCommodityCard(Commodity.GOLD, Commodity.IVORY));
        phase1.add(new LeaderCard("Charlemagne", 20, Advance.nationalism));
        phase1.add(new LeaderCard("Dionysus Exiguus", 20, Advance.writtenRecord));
        phase1.add(new LeaderCard("Rashid ad Din", 10, Advance.writtenRecord, Advance.overlandEast));
        phase1.add(new LeaderCard("St. Benedict", 10, Advance.writtenRecord, Advance.patronage));
        phase1.add(new LeaderCard("Walter the Penniless", 20, null, 10, Advance.overlandEast));
        phase1.add(new Card("Alchemist's Gold"));
        phase1.add(new Card("Civil War"));
        phase1.add(new Card("Enlightened Ruler"));
        phase1.add(new Card("Famine"));
        phase1.add(new Card("Mysticism Abounds"));
        phase1.add(new Card("Pirates / Vikings"));
        phase1.add(new Card("Rebellion"));
        phase1.add(new Card("Revolutionary Uprisings"));
        phase1.add(new Card("War"));
        final WeaponCard stirrups = new WeaponCard("Stirrups", 1);
        final WeaponCard armor = new WeaponCard("Armor", 2);
        final Card papalDecree = new Card("Papal Decree");
        final Card theCrusades = new Card("The Crusades");
        phase1.add(stirrups);
        phase1.add(armor);
        phase1.add(papalDecree);
        phase1.add(theCrusades);

        phase2.add(new CommodityCard(Commodity.TIMBER));
        phase2.add(new CommodityCard(Commodity.GRAIN));
        phase2.add(new CommodityCard(Commodity.GRAIN));
        phase2.add(new CommodityCard(Commodity.CLOTH));
        phase2.add(new CommodityCard(Commodity.WINE));
        phase2.add(new CommodityCard(Commodity.METAL));
        phase2.add(new CommodityCard(Commodity.SILK));
        phase2.add(new CommodityCard(Commodity.SPICE));
        phase2.add(new LeaderCard("Christopher Columbus", 30, Advance.oceanNavigation, Advance.newWorld));
        phase2.add(new LeaderCard("Desiderius Erasmus", 20, Advance.printedWord, Advance.renaissance));
        phase2.add(new LeaderCard("Ibn Majid", 20, Advance.oceanNavigation, Advance.cosmopolitan));
        phase2.add(new LeaderCard("Johann Gutenberg", 30, Advance.printedWord));
        phase2.add(new LeaderCard("Marco Polo", 20, null, 20, Advance.overlandEast, Advance.cosmopolitan));
        phase2.add(new LeaderCard("Nicolaus Copernicus", 20, Advance.heavens, Advance.institutionalResearch));
        phase2.add(new LeaderCard("Prince Henry", 20, Advance.oceanNavigation, Advance.institutionalResearch));
        phase2.add(new LeaderCard("William Caxton", 20, Advance.printedWord));
        phase2.add(new WeaponCard("Long Bow", 3).invalidates(stirrups, armor));
        phase2.add(new WeaponCard("Gunpowder", 4).invalidates(stirrups, armor));
        phase2.add(new Card("Black Death"));
        phase2.add(new Card("Mongol Armies").invalidates(theCrusades));
        phase2.add(new Card("Religious Strife").invalidates(papalDecree));

        phase3.add(new CommodityCard(Commodity.CLOTH));
        phase3.add(new CommodityCard(Commodity.WINE));
        phase3.add(new CommodityCard(Commodity.METAL));
        phase3.add(new CommodityCard(Commodity.FUR));
        phase3.add(new CommodityCard(Commodity.SILK));
        phase3.add(new CommodityCard(Commodity.SPICE));
        phase3.add(new CommodityCard(Commodity.GOLD));
        phase3.add(new LeaderCard("Andreas Vesalius", 20, Advance.humanBody, Advance.enlightenment));
        phase3.add(new LeaderCard("Bartolome de Las Casas", 30, Advance.cosmopolitan));
        phase3.add(new LeaderCard("Galileo Galilei", 20, Advance.heavens, Advance.renaissance));
        phase3.add(new LeaderCard("Henry Oldenburg", 30, Advance.enlightenment));
        phase3.add(new LeaderCard("Leonardo da Vinci", 20, Advance.masterArt, Advance.humanBody, Advance.renaissance));
        phase3.add(new LeaderCard("Sir Isaac Newton", 20, Advance.lawsOfMatter, Advance.enlightenment));
    }

    private final List<Player> players = new ArrayList<>();

    public enum Phase { ORDER_OF_PLAY, DRAW_CARD, BUY_CARD, PLAY_CARD, PURCHASE, EXPANSION, INCOME, FINAL_PLAY_CARD }

    public int getCommodityCount(Commodity commodity, Player player) {
        return 0;
    }

    public void commodityPlayed(Commodity commodity) {
        for (Player player : players) {
            int count = getCommodityCount(commodity, player);
            if (hasSurplus(commodity)) --count;
            if (hasShortage(commodity)) ++count;
            if (count > 0) {
                final int value = count * count * commodity.getValue();
            }
        }
    }

    public boolean hasShortage(Commodity commodity) {
        return false;
    }

    public boolean hasSurplus(Commodity commodity) {
        return false;
    }

    public LeaderCard getBestLeaderCard(Advance advance, Player player) {
        int maxDiscount = 0;
        LeaderCard bestCard = null;
        for (LeaderCard card : patronageQueue) {
            for (Advance discount : card.advances) {
                if (advance == discount) {
                    if (card.amount > maxDiscount) {
                        if (card.canUse(player)) {
                            maxDiscount = card.amount;
                            bestCard = card;
                        }
                    }
                }
            }
        }
        return bestCard;
    }
}
