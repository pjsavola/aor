package aor;

import message.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
public class Game {
    public enum Phase { DRAFT, SELECT_CAPITAL, ORDER_OF_PLAY, DRAW_CARD, BUY_CARD, PLAY_CARD, PURCHASE, EXPANSION, INCOME, FINAL_PLAY_CARD, END }

    private Phase phase;
    private final List<Card> delayedCards = new ArrayList<>();
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
    final List<Player> turnOrder;
    private final int playerCount;
    private int round = 1;
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

        delayedCards.add(theCrusades);
        delayedCards.add(walterThePenniless);
        delayedCards.add(rashidAdDin);
        delayedCards.add(new CommodityCard(Commodity.SILK));
        delayedCards.add(new CommodityCard(Commodity.SPICE));

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
        playerCount = players.size();
        turnOrder = new ArrayList<>(playerCount);
        phase = Phase.DRAFT;
    }

    private static class FutureOrDefault<T extends Response> {
        private final CompletableFuture<T> result;
        private final T fallback;
        private final Function<T, Boolean> requirement;

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
                case DRAFT -> draftPhase();
                case SELECT_CAPITAL -> selectCapitalPhase();
                case ORDER_OF_PLAY -> orderOfPlayPhase();
                case DRAW_CARD -> drawCardPhase();
                case BUY_CARD -> buyCardPhase();
                case PLAY_CARD -> playCardPhase();
                case PURCHASE -> purchasePhase();
                case EXPANSION -> expansionPhase();
                case INCOME -> incomePhase();
                case FINAL_PLAY_CARD -> finalPlayCardPhase();
            }
        }
    }

    private void queryForRenaissance() {
        for (int i = 0; i < turnOrder.size(); ++i) {
            final Player player = turnOrder.get(i);
            final Set<Integer> renaissanceOptions = getRenaissanceOptions(i, turnOrder, round);
            if (!renaissanceOptions.isEmpty()) {
                final int delta = new FutureOrDefault<>(
                        player.send(new UseRenaissanceRequest(getGameState())),
                        response -> response.getInt() == 0 || renaissanceOptions.contains(response.getInt()),
                        new IntegerResponse(0)).getResult().getInt();
                if (delta != 0) {
                    turnOrder.set(i, turnOrder.get(i + delta));
                    turnOrder.set(i + delta, player);
                    player.renaissanceUsed = round;
                }
            }
        }
    }

    private void draftPhase() {
        deck = epoch1;
        Collections.shuffle(deck, r);
        final List<FutureOrDefault<IntegerResponse>> asyncDiscards = new ArrayList<>(playerCount);
        final List<List<Card>> hands = new ArrayList<>(playerCount);
        for (Player player : players) {
            final List<Card> hand = List.of(drawCard(), drawCard(), drawCard());
            hands.add(hand);
            asyncDiscards.add(new FutureOrDefault<>(
                    player.send(new SelectCardRequest("Discard 1 card", hand)),
                    index -> index.getInt() >= 0 && index.getInt() < hand.size(),
                    new IntegerResponse(0)));
        }
        if (players.size() <= 4) {
            deck.addAll(delayedCards);
            delayedCards.clear();
        }
        for (int i = 0; i < playerCount; ++i) {
            final IntegerResponse response = asyncDiscards.get(i).getResult();
            final List<Card> hand = hands.get(i);
            for (int j = 0; j < hand.size(); ++j) {
                final Card card = hand.get(j);
                if (response.getInt() == j) {
                    deck.add(card);
                } else {
                    players.get(i).addCard(card);
                }
            }
        }
        Collections.shuffle(deck, r);
        phase = Phase.SELECT_CAPITAL;
    }

    private void selectCapitalPhase() {
        final List<Player> selectionOrder = new ArrayList<>(players);
        Collections.shuffle(selectionOrder);
        final List<FutureOrDefault<IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : selectionOrder) {
            asyncBids.add(new FutureOrDefault<>(
                    player.send(new BidForCapitalRequest()),
                    bid -> bid.getInt() >= 0 && bid.getInt() <= 40,
                    new IntegerResponse(0)));
        }
        final List<Integer> bids = new ArrayList<>(playerCount);
        final Set<Node.CityState> options = new HashSet<>(playerCount);
        asyncBids.stream().map(FutureOrDefault::getResult).mapToInt(IntegerResponse::getInt).forEach(bids::add);
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
            turnOrder.add(player);
        }
        for (Player player : turnOrder) {
            final Node.CityState capital = new FutureOrDefault<>(
                    player.send(new SelectCapitalRequest(getGameState())),
                    response -> options.contains(response.getCapital()),
                    new CapitalResponse(options.iterator().next())).getResult().getCapital();
            player.selectCapital(capital);
            options.remove(capital);
        }
        phase = Phase.ORDER_OF_PLAY;
    }

    private void orderOfPlayPhase() {
        turnOrder.clear();
        final GameState gameState = getGameState();
        final List<FutureOrDefault<IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : players) {
            asyncBids.add(new FutureOrDefault<>(
                    player.send(new BidForTurnOrderRequest(gameState)),
                    bid -> Math.abs(bid.getInt()) <= player.getCash(),
                    new IntegerResponse(0)));
        }
        final List<Integer> bids = new ArrayList<>(playerCount);
        asyncBids.stream().map(FutureOrDefault::getResult).mapToInt(IntegerResponse::getInt).forEach(bids::add);
        while (turnOrder.size() < playerCount) {
            int lowestBid = Integer.MAX_VALUE;
            int index = -1;
            for (int i = 0; i < bids.size(); ++i) {
                if (bids.get(i) <= lowestBid) {
                    lowestBid = bids.get(i);
                    index = i;
                }
            }
            bids.set(index, Integer.MAX_VALUE);
            final Player player = players.get(index);
            player.adjustCash(-Math.abs(lowestBid));
            player.writtenCash = player.getCash();
            player.addTokens(lowestBid);
            turnOrder.add(players.get(index));
        }
        phase = round == 1 ? Phase.PLAY_CARD : Phase.DRAW_CARD;
    }

    private void drawCardPhase() {
        queryForRenaissance();
        while (!surpluses.isEmpty() || !shortages.isEmpty()) {
            final Player player = turnOrder.get(0);
            final Set<Commodity> options = new HashSet<>();
            for (Commodity commodity : surpluses) if (player.getCash() >= commodity.getValue()) options.add(commodity);
            for (Commodity commodity : shortages) if (player.getCash() >= commodity.getValue()) options.add(commodity);
            if (!options.isEmpty()) {
                final CommodityReponse commodityReponse = new FutureOrDefault<>(
                        player.send(new AdjustShortageSurplusRequest(getGameState(), "Pay off shortage/surplus?")),
                        response -> response.getCommodity() == null || options.contains(response.getCommodity()),
                        new CommodityReponse(null)).getResult();
                final Commodity selectedCommodity = commodityReponse.getCommodity();
                if (selectedCommodity == null || commodityReponse.getAdjustment() == 0) {
                    break;
                } else {
                    player.adjustCash(-selectedCommodity.getValue());
                    if (commodityReponse.getAdjustment() > 0) surpluses.remove(selectedCommodity);
                    else shortages.remove(selectedCommodity);
                }
            }
        }
        if (turnOrder.get(turnOrder.size() - 1).getAdvances().contains(Advance.windWaterMill)) {
            final Player player = turnOrder.get(turnOrder.size() - 1);
            final CommodityReponse commodityReponse = new FutureOrDefault<>(
                    player.send(new AdjustShortageSurplusRequest(getGameState(), "Adjust shortage/surplus?")),
                    response -> response.getCommodity() == null || (response.getCommodity().getValue() >= Commodity.GRAIN.getValue() && response.getCommodity().getValue() <= Commodity.METAL.getValue()),
                    new CommodityReponse(null)).getResult();
            final Commodity selectedCommodity = commodityReponse.getCommodity();
            final int adjustment = commodityReponse.getAdjustment();
            if (selectedCommodity != null && adjustment != 0) {
                if (adjustment > 0) {
                    if (!surpluses.remove(selectedCommodity)) shortages.add(selectedCommodity);
                } else {
                    if (!shortages.remove(selectedCommodity)) surpluses.add(selectedCommodity);
                }
            }
        }
        for (Player player : turnOrder) {
            final Card c = drawCard();
            if (c != null) {
                player.notify(new CardNotification(c));
            }
        }
        phase = Phase.BUY_CARD;
    }

    private void buyCardPhase() {
        queryForRenaissance();
        for (Player player : turnOrder) {
            if (player.getCash() >= 10 && deck.isEmpty() && player.getAdvances().contains(Advance.urbanAscendancy)) {
                if (new FutureOrDefault<>(
                        player.send(new UseUrbanAscendancyRequest(getGameState())),
                        response -> true,
                        new BooleanResponse(false)).getResult().getBool()) {
                    final Card c = drawCard();
                    player.adjustCash(-10);
                    player.notify(new CardNotification(c));
                }
            }
        }
        final List<FutureOrDefault<IntegerResponse>> masterArtResponses = new ArrayList<>();
        for (Player player : turnOrder) {
            if (!player.cards.isEmpty() && player.getAdvances().contains(Advance.masterArt)) {
                masterArtResponses.add(new FutureOrDefault<>(
                        player.send(new SelectCardRequest("Discard 1 card?", player.cards)),
                        index -> index.getInt() >= -1 && index.getInt() < player.cards.size(),
                        new IntegerResponse(-1)));
            } else {
                masterArtResponses.add(null);
            }
        }
        for (int i = 0; i < playerCount; ++i) {
            if (masterArtResponses.get(i) != null) {
                final Player player = turnOrder.get(i);
                final int index = masterArtResponses.get(i).getResult().getInt();
                if (index != -1) {
                    final Card card = player.cards.remove(index);
                    playedCards.add(card);
                }
            }
        }
        phase = Phase.PLAY_CARD;
    }

    private void playCardPhase() {
        queryForRenaissance();
        for (Player player : turnOrder) {
            while (!player.cards.isEmpty()) {
                final int cardIndex = new FutureOrDefault<>(
                        player.send(new SelectCardRequest("PLay 1 card?", player.cards)),
                        index -> index.getInt() >= -1 && index.getInt() < player.cards.size(),
                        new IntegerResponse(-1)).getResult().getInt();
                if (cardIndex == -1) {
                    break;
                } else {
                    final Card card = player.cards.remove(cardIndex);
                    card.play(this, player);
                }
            }
        }
        phase = Phase.PURCHASE;
    }

    private void purchasePhase() {
        queryForRenaissance();
        for (Player player : turnOrder) {

        }
        purchasePhaseFinished();
        phase = Phase.EXPANSION;
    }

    private void expansionPhase() {
        queryForRenaissance();
        phase = Phase.INCOME;
    }

    private void incomePhase() {
        for (Player player : players) {
            player.getIncome(playerCount);
        }
        if (deck.isEmpty()) {
            phase = Phase.FINAL_PLAY_CARD;
        } else {
            if (round++ == 2 && !delayedCards.isEmpty()) {
                deck.addAll(delayedCards);
                delayedCards.clear();
                Collections.shuffle(deck, r);
            }
            phase = Phase.ORDER_OF_PLAY;
        }
    }

    private void finalPlayCardPhase() {
        for (Player player : turnOrder) {
            player.cards.removeIf(c -> !c.canPlay(this));
            while (!player.cards.isEmpty()) {
                final int cardIndex = new FutureOrDefault<>(
                        player.send(new SelectCardRequest("PLay 1 cards", player.cards)),
                        index -> index.getInt() >= -0 && index.getInt() < player.cards.size(),
                        new IntegerResponse(0)).getResult().getInt();
                final Card card = player.cards.remove(cardIndex);
                card.play(this, player);
            }
        }
        phase = Phase.END;
    }

    private static Set<Integer> getRenaissanceOptions(int index, List<Player> turnOrder, int round) {
        final Set<Integer> options = new HashSet<>();
        final Player player = turnOrder.get(index);
        if (player.renaissanceUsed < round && turnOrder.size() > 1 && player.getAdvances().contains(Advance.renaissance)) {
            if (index > 0 && !turnOrder.get(index - 1).getAdvances().contains(Advance.renaissance)) options.add(-1);
            if (index < turnOrder.size() - 1 && !turnOrder.get(index + 1).getAdvances().contains(Advance.renaissance)) options.add(1);
        }
        return options;
    }

    private GameState getGameState() {
        final GameState state = new GameState();
        state.epoch = getEpoch();
        state.deckSize = deck.size();
        state.phase = phase;
        state.round = round;
        turnOrder.forEach(p -> state.turnOrder.add(p.getState()));
        state.shortages.addAll(shortages);
        state.surpluses.addAll(surpluses);
        return state;
    }

    private int getEpoch() {
        if (deck != null) {
            if (deck.isEmpty()) return 4;
            if (deck == epoch1) return 1;
            if (deck == epoch2) return 2;
            if (deck == epoch3) return 3;
        }
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
            int count = player.getCommodityCount(commodity);
            if (shortages.remove(commodity)) ++count;
            if (surpluses.remove(commodity)) --count;
            if (count > 0) {
                final int value = count * count * commodity.getValue();
                player.adjustCash(value);
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
