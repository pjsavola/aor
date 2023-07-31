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
        initDecks();
        players.add(new Player(this));
        playerCount = players.size();
        turnOrder = new ArrayList<>(playerCount);
        phase = Phase.DRAFT;
    }

    private void initDecks() {
        epoch1.add(Cards.stirrups);
        epoch1.add(Cards.armor);
        epoch1.add(Cards.stone1);
        epoch1.add(Cards.stone2);
        epoch1.add(Cards.wool1);
        epoch1.add(Cards.wool2);
        epoch1.add(Cards.timber1);
        epoch1.add(Cards.timber2);
        epoch1.add(Cards.clothWine);
        epoch1.add(Cards.metal1);
        epoch1.add(Cards.fur1);
        epoch1.add(Cards.goldIvory);
        epoch1.add(Cards.charlemagne);
        epoch1.add(Cards.dionysusExiguus);
        epoch1.add(Cards.stBenedict);
        epoch1.add(Cards.alchemistsGold);
        epoch1.add(Cards.civilWar);
        epoch1.add(Cards.enlightenedRuler);
        epoch1.add(Cards.famine);
        epoch1.add(Cards.mysticismAbounds);
        epoch1.add(Cards.papalDecree);
        epoch1.add(Cards.piratesVikings);
        epoch1.add(Cards.rebellion);
        epoch1.add(Cards.revolutionaryUprisings);
        epoch1.add(Cards.war);

        delayedCards.add(Cards.theCrusades);
        delayedCards.add(Cards.walterThePenniless);
        delayedCards.add(Cards.rashidAdDin);
        delayedCards.add(Cards.silk1);
        delayedCards.add(Cards.spice1);

        epoch2.add(Cards.longBow);
        epoch2.add(Cards.gunpowder);
        epoch2.add(Cards.timber3);
        epoch2.add(Cards.grain1);
        epoch2.add(Cards.grain2);
        epoch2.add(Cards.cloth1);
        epoch2.add(Cards.wine1);
        epoch2.add(Cards.metal2);
        epoch2.add(Cards.silk2);
        epoch2.add(Cards.spice2);
        epoch2.add(Cards.christopherColumbus);
        epoch2.add(Cards.desideriusErasmus);
        epoch2.add(Cards.ibnMajid);
        epoch2.add(Cards.johannGutenberg);
        epoch2.add(Cards.marcoPolo);
        epoch2.add(Cards.nicolausCopernicus);
        epoch2.add(Cards.princeHenry);
        epoch2.add(Cards.williamCaxton);
        epoch2.add(Cards.mongolArmies);
        epoch2.add(Cards.blackDeath);
        epoch2.add(Cards.religiousStrife);

        epoch3.add(Cards.cloth2);
        epoch3.add(Cards.wine2);
        epoch3.add(Cards.metal3);
        epoch3.add(Cards.fur2);
        epoch3.add(Cards.silk3);
        epoch3.add(Cards.spice3);
        epoch3.add(Cards.gold);
        epoch3.add(Cards.andreasVesalius);
        epoch3.add(Cards.bartolomeDeLasCasas);
        epoch3.add(Cards.galileoGalilei);
        epoch3.add(Cards.henryOldenburg);
        epoch3.add(Cards.leonardoDaVinci);
        epoch3.add(Cards.sirIsaacNewton);
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
        int adjustment = 0;
        if (surpluses.remove(commodity)) --adjustment;
        if (shortages.remove(commodity)) ++adjustment;
        for (Player player : players) {
            int count = player.getCommodityCount(commodity);
            if (count > 0) {
                if (player.getAdvances().contains(Advance.industry)) ++count;
                count += adjustment;
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
