package aor;

import message.*;

import java.util.*;
import java.util.stream.Collectors;

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
    Advance.Category bannedCategory;
    final List<Player> turnOrder;
    private final int playerCount;
    private int round = 1;
    Player enlightenedRuler;
    Player civilWar;
    private final Random r = new Random();
    public List<Node> nodes;
    public Player war1;
    public Player war2;

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

    private void draftPhase() {
        deck = epoch1;
        Collections.shuffle(deck, r);
        final List<FutureOrDefault<SelectCardRequest, IntegerResponse>> asyncDiscards = new ArrayList<>(playerCount);
        final List<List<Card>> hands = new ArrayList<>(playerCount);
        for (Player player : players) {
            final List<Card> hand = List.of(drawCard(), drawCard(), drawCard());
            hands.add(hand);
            asyncDiscards.add(new FutureOrDefault<>(player, new SelectCardRequest("Discard 1 card", hand, false)));
        }
        if (players.size() <= 4) {
            deck.addAll(delayedCards);
            delayedCards.clear();
        }
        for (int i = 0; i < playerCount; ++i) {
            final int index = asyncDiscards.get(i).get().getInt();
            final List<Card> hand = hands.get(i);
            for (int j = 0; j < hand.size(); ++j) {
                final Card card = hand.get(j);
                if (index == j) {
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
        final List<FutureOrDefault<BidForCapitalRequest, IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : selectionOrder) {
            asyncBids.add(new FutureOrDefault<>(player, new BidForCapitalRequest()));
        }
        final List<Integer> bids = new ArrayList<>(playerCount);
        final Set<Node.CityState> options = new HashSet<>(playerCount);
        asyncBids.stream().map(FutureOrDefault::get).mapToInt(IntegerResponse::getInt).forEach(bids::add);
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
            final Node.CityState capital = new FutureOrDefault<>(player, new SelectCapitalRequest("Select Power to play", getGameState(), options)).get().getCapital();
            player.selectCapital(capital);
            options.remove(capital);
        }
        phase = Phase.ORDER_OF_PLAY;
    }

    private void orderOfPlayPhase() {
        turnOrder.clear();
        final GameState gameState = getGameState();
        final List<FutureOrDefault<BidForTurnOrderRequest, IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : players) {
            asyncBids.add(new FutureOrDefault<>(player, new BidForTurnOrderRequest(gameState, player.getCash())));
        }
        final List<Integer> bids = new ArrayList<>(playerCount);
        asyncBids.stream().map(FutureOrDefault::get).mapToInt(IntegerResponse::getInt).forEach(bids::add);
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
                final CommodityReponse commodityReponse = new FutureOrDefault<>(player, new AdjustShortageSurplusRequest("Pay off shortage/surplus?", getGameState(), options)).get();
                final Commodity selectedCommodity = commodityReponse.getCommodity();
                if (selectedCommodity == null) {
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
            final Set<Commodity> options = Set.of(Commodity.GRAIN, Commodity.CLOTH, Commodity.WINE, Commodity.METAL);
            final CommodityReponse commodityReponse = new FutureOrDefault<>(player, new AdjustShortageSurplusRequest("Adjust shortage/surplus?", getGameState(), options)).get();
            final Commodity selectedCommodity = commodityReponse.getCommodity();
            if (selectedCommodity != null) {
                if (commodityReponse.getAdjustment() > 0) {
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
                if (new FutureOrDefault<>(player, new UseUrbanAscendancyRequest(getGameState())).get().getBool()) {
                    final Card c = drawCard();
                    player.adjustCash(-10);
                    player.notify(new CardNotification(c));
                }
            }
        }
        final List<FutureOrDefault<SelectCardRequest, IntegerResponse>> masterArtResponses = new ArrayList<>();
        for (Player player : turnOrder) {
            if (!player.cards.isEmpty() && player.getAdvances().contains(Advance.masterArt)) {
                masterArtResponses.add(new FutureOrDefault<>(player, new SelectCardRequest("Discard 1 card with Master Art?", player.cards, true)));
            } else {
                masterArtResponses.add(null);
            }
        }
        for (int i = 0; i < playerCount; ++i) {
            if (masterArtResponses.get(i) != null) {
                final Player player = turnOrder.get(i);
                final int index = masterArtResponses.get(i).get().getInt();
                if (index != -1) {
                    final Card card = player.cards.remove(index);
                    moveToNextDeck(card);
                }
            }
        }
        phase = Phase.PLAY_CARD;
    }

    private void playCardPhase() {
        queryForRenaissance();
        for (Player player : turnOrder) {
            resolveWar(player);
            while (!player.cards.isEmpty()) {
                final List<Card> playableCards = player.cards.stream().filter(c -> c.canPlay(this)).toList();
                final int cardIndex = new FutureOrDefault<>(player, new SelectCardRequest("PLay 1 card?", playableCards, true)).get().getInt();
                if (cardIndex == -1) {
                    break;
                } else {
                    final Card card = playableCards.get(cardIndex);
                    player.cards.remove(card);
                    card.play(this, player);
                }
            }
        }
        phase = Phase.PURCHASE;
    }

    private void purchasePhase() {
        queryForRenaissance();
        for (int i = 0; i < turnOrder.size(); ++i) {
            final Player player = turnOrder.get(i);
            final List<Advance> advances = new FutureOrDefault<>(player, new PurchaseAdvancesRequest(getGameState(), i)).get().getAdvances();
            for (Advance advance : advances) {
                player.research(advance);
            }
        }
        purchasePhaseFinished();
        phase = Phase.EXPANSION;
    }

    private void expansionPhase() {
        if (civilWar != null) {
            turnOrder.remove(civilWar);
            turnOrder.add(civilWar);
            civilWar = null;
        }
        queryForRenaissance();
        phase = Phase.INCOME;
    }

    private void incomePhase() {
        int mostTokens = 0;
        Player winningPlayer = null;
        for (Player player : players) {
            player.weapons.clear();
            final int tokens = player.flipTokens();
            if (tokens > mostTokens) {
                mostTokens = tokens;
                winningPlayer = player;
            }
            player.adjustCash(player.getIncome(playerCount));
        }
        if (winningPlayer != null) {
            final Card c = drawCard();
            if (c != null) {
                winningPlayer.notify(new CardNotification(c));
            }
        }
        if (deck.isEmpty()) {
            phase = Phase.FINAL_PLAY_CARD;
        } else {
            bannedCategory = null;
            enlightenedRuler = null;
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
            resolveWar(player);
            player.cards.removeIf(c -> !c.canPlay(this));
            while (!player.cards.isEmpty()) {
                final int cardIndex = new FutureOrDefault<>(player, new SelectCardRequest("Play 1 card", player.cards, false)).get().getInt();
                final Card card = player.cards.remove(cardIndex);
                card.play(this, player);
            }
        }
        phase = Phase.END;
    }

    private void queryForRenaissance() {
        for (int i = 0; i < turnOrder.size(); ++i) {
            final Player player = turnOrder.get(i);
            final Set<Integer> renaissanceOptions = getRenaissanceOptions(i, turnOrder, round);
            if (!renaissanceOptions.isEmpty()) {
                final int delta = new FutureOrDefault<>(player, new UseRenaissanceRequest(getGameState(), renaissanceOptions)).get().getInt();
                if (delta != 0) {
                    turnOrder.set(i, turnOrder.get(i + delta));
                    turnOrder.set(i + delta, player);
                    player.renaissanceUsed = round;
                }
            }
        }
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

    public void resolveWar(Player currentPlayer) {
        if (war1 != null && war2 != null) {
            if (war1.chaos || war2.chaos) {
                war1 = null;
                war2 = null;
                return;
            }
            war1.adjustMisery(1);
            war2.adjustMisery(1);
            int bestWeapon1 = war1.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
            int bestWeapon2 = war2.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
            if (currentPlayer == war1 || currentPlayer == war2) {
                final int opponentBestWeapon = currentPlayer == war1 ? bestWeapon2 : bestWeapon1;
                final List<WeaponCard> playableWeapons = new ArrayList<>(currentPlayer.cards.stream()
                        .filter(c -> c instanceof WeaponCard && c.canPlay(this))
                        .map(c -> (WeaponCard) c)
                        .filter(c -> c.power > opponentBestWeapon).toList());
                while (!playableWeapons.isEmpty()) {
                    final int index = new FutureOrDefault<>(currentPlayer, new SelectCardRequest("Play weapons for war?", playableWeapons, true)).get().getInt();
                    if (index == -1) {
                        break;
                    } else {
                        final WeaponCard card = playableWeapons.remove(index);
                        card.play(this, currentPlayer);
                        bestWeapon1 = war1.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
                        bestWeapon2 = war2.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
                    }
                }
            }
            final int mod1 = bestWeapon1 > bestWeapon2 ? war1.weapons.size() : 0;
            final int mod2 = bestWeapon2 > bestWeapon1 ? war2.weapons.size() : 0;
            final int roll1 = r.nextInt(6) + mod1;
            final int roll2 = r.nextInt(6) + mod2;
            if (roll1 != roll2) {
                final Player winner =  roll1 > roll2 ? war1 : war2;
                final Player loser = roll1 > roll2 ? war2 : war1;
                final Set<String> options = loser.cities.stream().filter(n -> n.getRegion() != 5 || winner.getAdvances().contains(Advance.overlandEast)).map(Node::getName).collect(Collectors.toSet());
                final int count = Math.abs(roll1 - roll2);
                if (!options.isEmpty()) {
                    final String[] targets;
                    if (options.size() > count) {
                        targets = new FutureOrDefault<>(loser, new SelectTargetCitiesRequest("Choose cities to lose in War!", getGameState(), options, Math.min(count, options.size()))).get().getCities();
                    } else {
                        targets = options.toArray(String[]::new);
                    }
                    for (String target : targets) {
                        loser.cities.stream().filter(c -> c.getName().equals(target)).forEach(n -> {
                            loser.remove(n);
                            winner.cities.add(n);
                        });
                    }
                }
                loser.adjustMisery(1);
                loser.adjustMisery(Math.max(0, count - options.size()));
                war1 = null;
                war2 = null;
            } else if (mod1 != mod2) {
                if (mod1 > mod2) war2.adjustMisery(1);
                else war1.adjustMisery(1);
                war1 = null;
                war2 = null;
            }
        }
    }

    public GameState getGameState() {
        final GameState state = new GameState();
        state.epoch = getEpoch();
        state.deckSize = deck.size();
        state.phase = phase;
        state.round = round;
        state.shortages.addAll(shortages);
        state.surpluses.addAll(surpluses);
        state.bannedCategory = bannedCategory;
        state.playedCards = playedCards.stream().mapToInt(Card::getIndex).toArray();
        turnOrder.forEach(p -> state.turnOrder.add(p.getState()));
        state.patronageCards = new int[patronageQueue.size()];
        state.patronageUsesRemaining = new int[patronageQueue.size()];
        for (int i = 0; i < patronageQueue.size(); ++i) {
            state.patronageCards[i] = patronageQueue.get(i).getIndex();
            state.patronageUsesRemaining[i] = patronageQueue.get(i).usesRemaining;
            final int playerIdx = turnOrder.indexOf(patronageQueue.get(i).owner);
            state.turnOrder.get(playerIdx).ownedPatronageCards.add(state.patronageCards[i]);
        }
        return state;
    }

    public int getEpoch() {
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

    private void purchasePhaseFinished() {
        for (Player player : players) {
            player.purchasePhaseFinished();
        }
        patronageQueue.clear();
        final Iterator<Card> it = playedCards.iterator();
        while (it.hasNext()) {
            final Card card = it.next();
            if (card == Cards.war && war1 != null && war2 != null) continue;
            moveToNextDeck(card);
            it.remove();
        }
    }

    public void moveToNextDeck(Card card) {
        if (!card.singleUse) {
            if (deck == epoch1) epoch2.add(card);
            if (deck == epoch2) epoch3.add(card);
        }
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

    public LeaderCard getBestLeaderCard(Advance advance, Player player, Set<Advance> allAdvances) {
        int maxDiscount = 0;
        LeaderCard bestCard = null;
        for (LeaderCard card : patronageQueue) {
            for (Advance discount : card.advances) {
                if (advance == discount) {
                    final int amount = card.getAmount(playedCards);
                    if (amount > maxDiscount) {
                        if (card.canUse(player, allAdvances)) {
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
