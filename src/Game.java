import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class Game {
    public enum Phase { DRAFT, SELECT_CAPITAL, ORDER_OF_PLAY, DRAW_CARD, BUY_CARD, PLAY_CARD, PURCHASE, EXPANSION, INCOME, FINAL_PLAY_CARD, END }

    private Phase phase;
    private final List<Card> sideCards = new ArrayList<>();
    private final List<Card> epoch1 = new ArrayList<>();
    private final List<Card> epoch2 = new ArrayList<>();
    private final List<Card> epoch3 = new ArrayList<>();
    private List<Card> deck;
    final List<LeaderCard> patronageQueue = new ArrayList<>();
    final Set<Card> playedCards = new HashSet<>();
    final Set<Card> unplayableCards = new HashSet<>();
    private final List<Commodity> shortages = new ArrayList<>();
    private final List<Commodity> surpluses = new ArrayList<>();
    final List<Player> players = new ArrayList<>();
    private Player current;
    private final Random r = new Random();

    public Game() {
        final WeaponCard stirrups = new WeaponCard("Stirrups", 1);
        final WeaponCard armor = new WeaponCard("Armor", 2);
        final Card papalDecree = new EventCard(EventCard.Type.PAPAL_DECREE);
        final Card theCrusades = new EventCard(EventCard.Type.THE_CRUSADES);
        final Card rashidAdDin = new LeaderCard("Rashid ad Din", 10, Advance.writtenRecord, Advance.overlandEast);
        final Card walterThePenniless = new LeaderCard("Walter the Penniless", 20, theCrusades, 10, Advance.overlandEast);
        epoch1.add(stirrups);
        epoch1.add(armor);
        epoch1.add(papalDecree);
        epoch1.add(new CommodityCard(Commodity.STONE));
        epoch1.add(new CommodityCard(Commodity.STONE));
        epoch1.add(new CommodityCard(Commodity.WOOL));
        epoch1.add(new CommodityCard(Commodity.WOOL));
        epoch1.add(new CommodityCard(Commodity.TIMBER));
        epoch1.add(new CommodityCard(Commodity.TIMBER));
        epoch1.add(new DoubleCommodityCard(Commodity.CLOTH, Commodity.WINE));
        epoch1.add(new CommodityCard(Commodity.METAL));
        epoch1.add(new CommodityCard(Commodity.FUR));
        epoch1.add(new DoubleCommodityCard(Commodity.GOLD, Commodity.IVORY));
        epoch1.add(new LeaderCard("Charlemagne", 20, Advance.nationalism));
        epoch1.add(new LeaderCard("Dionysus Exiguus", 20, Advance.writtenRecord));
        epoch1.add(new LeaderCard("St. Benedict", 10, Advance.writtenRecord, Advance.patronage));
        epoch1.add(new EventCard(EventCard.Type.ALCHEMISTS_GOLD));
        epoch1.add(new EventCard(EventCard.Type.CIVIL_WAR));
        epoch1.add(new EventCard(EventCard.Type.ENLIGHTENED_RULER));
        epoch1.add(new EventCard(EventCard.Type.FAMINE));
        epoch1.add(new EventCard(EventCard.Type.MYSTICISM_ABOUNDS));
        epoch1.add(new EventCard(EventCard.Type.PIRATES_VIKINGS));
        epoch1.add(new EventCard(EventCard.Type.REBELLION));
        epoch1.add(new EventCard(EventCard.Type.REVOLUTIONARY_UPRISINGS));
        epoch1.add(new EventCard(EventCard.Type.WAR));

        sideCards.add(theCrusades);
        sideCards.add(walterThePenniless);
        sideCards.add(rashidAdDin);
        sideCards.add(new CommodityCard(Commodity.SILK));
        sideCards.add(new CommodityCard(Commodity.SPICE));

        final Card mongolArmies = new EventCard(EventCard.Type.MONGOL_ARMIES).invalidates(theCrusades);
        epoch2.add(mongolArmies);
        epoch2.add(new CommodityCard(Commodity.TIMBER));
        epoch2.add(new CommodityCard(Commodity.GRAIN));
        epoch2.add(new CommodityCard(Commodity.GRAIN));
        epoch2.add(new CommodityCard(Commodity.CLOTH));
        epoch2.add(new CommodityCard(Commodity.WINE));
        epoch2.add(new CommodityCard(Commodity.METAL));
        epoch2.add(new CommodityCard(Commodity.SILK));
        epoch2.add(new CommodityCard(Commodity.SPICE));
        epoch2.add(new LeaderCard("Christopher Columbus", 30, Advance.oceanNavigation, Advance.newWorld));
        epoch2.add(new LeaderCard("Desiderius Erasmus", 20, Advance.printedWord, Advance.renaissance));
        epoch2.add(new LeaderCard("Ibn Majid", 20, Advance.oceanNavigation, Advance.cosmopolitan));
        epoch2.add(new LeaderCard("Johann Gutenberg", 30, Advance.printedWord));
        epoch2.add(new LeaderCard("Marco Polo", 20, mongolArmies, 20, Advance.overlandEast, Advance.cosmopolitan));
        epoch2.add(new LeaderCard("Nicolaus Copernicus", 20, Advance.heavens, Advance.institutionalResearch));
        epoch2.add(new LeaderCard("Prince Henry", 20, Advance.oceanNavigation, Advance.institutionalResearch));
        epoch2.add(new LeaderCard("William Caxton", 20, Advance.printedWord));
        epoch2.add(new WeaponCard("Long Bow", 3).invalidates(stirrups, armor));
        epoch2.add(new WeaponCard("Gunpowder", 4).invalidates(stirrups, armor));
        epoch2.add(new EventCard(EventCard.Type.BLACK_DEATH));
        epoch2.add(new EventCard(EventCard.Type.RELIGIOUS_STRIFE).invalidates(papalDecree));

        epoch3.add(new CommodityCard(Commodity.CLOTH));
        epoch3.add(new CommodityCard(Commodity.WINE));
        epoch3.add(new CommodityCard(Commodity.METAL));
        epoch3.add(new CommodityCard(Commodity.FUR));
        epoch3.add(new CommodityCard(Commodity.SILK));
        epoch3.add(new CommodityCard(Commodity.SPICE));
        epoch3.add(new CommodityCard(Commodity.GOLD));
        epoch3.add(new LeaderCard("Andreas Vesalius", 20, Advance.humanBody, Advance.enlightenment));
        epoch3.add(new LeaderCard("Bartolome de Las Casas", 30, Advance.cosmopolitan));
        epoch3.add(new LeaderCard("Galileo Galilei", 20, Advance.heavens, Advance.renaissance));
        epoch3.add(new LeaderCard("Henry Oldenburg", 30, Advance.enlightenment));
        epoch3.add(new LeaderCard("Leonardo da Vinci", 20, Advance.masterArt, Advance.humanBody, Advance.renaissance));
        epoch3.add(new LeaderCard("Sir Isaac Newton", 20, Advance.lawsOfMatter, Advance.enlightenment));

        players.add(new Player(this));
        phase = Phase.DRAFT;
    }

    private static class FutureOrDefault<T> {
        private final CompletableFuture<T> result;
        private final T fallback;
        private Function<T, Boolean> requirement;

        private FutureOrDefault(CompletableFuture<T> result, Function<T, Boolean> requirement, T fallback) {
            this.result = result;
            this.requirement = requirement;
            this.fallback = fallback;
        }

        private T getResult() {
            final T result = get();
            return requirement.apply(result) ? result : fallback;
        }

        private T get() {
            try {
                return result.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return fallback;
            }
        }
    }

    public void run() {
        while (phase != Phase.END) {
            switch (phase) {
                case DRAFT -> {
                    deck = epoch1;
                    Collections.shuffle(deck, r);
                    final List<FutureOrDefault<Card>> asyncDiscards = new ArrayList<>();
                    for (Player player : players) {
                        final Card c1 = drawCard();
                        final Card c2 = drawCard();
                        final Card c3 = drawCard();
                        asyncDiscards.add(new FutureOrDefault<>(player.getInput(() -> player.discardOne(c1, c2, c3)), c -> c == c1 || c == c2 || c == c3, c3));
                    }
                    if (players.size() <= 4) {
                        deck.addAll(sideCards);
                        sideCards.clear();
                    }
                    asyncDiscards.stream().map(FutureOrDefault::getResult).forEach(deck::add);
                    Collections.shuffle(deck, r);
                }
                case SELECT_CAPITAL -> {
                    final List<Player> selectionOrder = new ArrayList<>(players);
                    Collections.shuffle(selectionOrder);
                    final List<FutureOrDefault<Integer>> asyncBids = new ArrayList<>();
                    for (Player player : selectionOrder) {
                        asyncBids.add(new FutureOrDefault<>(player.getInput(player::bidForCapital), bid -> bid >= 0 && bid <= 40, 0));
                    }
                    final List<Integer> bids = new ArrayList<>(selectionOrder.size());
                    final Set<Node.CityState> options = new HashSet<>(selectionOrder.size());
                    asyncBids.stream().map(FutureOrDefault::getResult).forEach(bids::add);
                    players.clear();
                    while (!selectionOrder.isEmpty()) {
                        int highestBid = -1;
                        int index = -1;
                        for (int i = 0; i < bids.size(); ++i) {
                            if (bids.get(i) > highestBid) {
                                highestBid = bids.get(i);
                                index = i;
                            }
                        }
                        bids.remove(index);
                        final Player player = selectionOrder.remove(index);
                        player.adjustCash(-highestBid);
                        options.add(Node.CityState.values()[players.size()]);
                        players.add(player);
                    }
                    for (Player player : players) {
                        options.remove(new FutureOrDefault<>(player.getInput(() -> player.selectCapital(getGameState())), options::contains, options.iterator().next()).getResult());
                    }
                }
            }
        }
    }

    private GameState getGameState() {
        final GameState state = new GameState();
        players.forEach(p -> state.playerStates.add(p.getState()));
        return state;
    }

    public int getCommodityCount(Commodity commodity, Player player) {
        return 0;
    }

    public Card drawCard() {
        Card card = null;
        if (!deck.isEmpty()) {
            card = deck.remove(deck.size() - 1);
        }
        if (deck.isEmpty()) {
            if (deck == epoch1) deck = epoch2;
            else if (deck == epoch2) deck = epoch3;
            Collections.shuffle(deck, r);
        }
        return card;
    }

    public void purchasePhaseFinished() {
        for (Player player : players) {
            player.purchasePhaseFinished();
        }
        patronageQueue.clear();
        for (Card card : playedCards) {
            if (!card.singleUse) {
                if (deck == epoch1) epoch2.add(card);
                if (deck == epoch2) epoch3.add(card);
            }
        }
        playedCards.clear();
    }

    public void commodityPlayed(Commodity commodity) {
        for (Player player : players) {
            int count = getCommodityCount(commodity, player);
            if (shortages.remove(commodity)) ++count;
            if (surpluses.remove(commodity)) --count;
            if (count > 0) {
                final int value = count * count * commodity.getValue();
            }
        }
    }

    public LeaderCard getBestLeaderCard(Advance advance, Player player) {
        int maxDiscount = 0;
        LeaderCard bestCard = null;
        for (LeaderCard card : patronageQueue) {
            for (Advance discount : card.advances) {
                if (advance == discount) {
                    final int amount = card.getAmount(this);
                    if (amount > maxDiscount) {
                        if (card.canUse(player)) {
                            maxDiscount = amount;
                            bestCard = card;
                        }
                    }
                }
            }
        }
        return bestCard;
    }
}
