package aor;

import message.*;

import java.util.*;
import java.util.stream.Collectors;

public class Server implements Runnable {
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
    public Player war1;
    public Player war2;

    public Server(List<ClientConnection> clients) {
        initDecks();
        this.playerCount = clients.size();
        for (int i = 0; i < playerCount; ++i) {
            players.add(new Player(this, clients.get(i), playerCount));
        }
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

    @Override
    public void run() {
        while (phase != Phase.END) {
            log(phase + " begins");
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
            asyncDiscards.add(new FutureOrDefault<>(player, new SelectCardRequest("Discard 1 card", null, hand, false), true));
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
        log("Initial draft completed");
        if (players.size() <= 4) {
            log("Delayed cards " + delayedCards.stream().map(Card::getName).collect(Collectors.joining(", ")) + " added to the deck");
            deck.addAll(delayedCards);
            delayedCards.clear();
        }
        log("Deck 1 is shuffled");
        Collections.shuffle(deck, r);
        phase = Phase.SELECT_CAPITAL;
    }

    private void selectCapitalPhase() {
        final List<Player> selectionOrder = new ArrayList<>(players);
        Collections.shuffle(selectionOrder);
        final List<FutureOrDefault<BidForCapitalRequest, IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : selectionOrder) {
            asyncBids.add(new FutureOrDefault<>(player, new BidForCapitalRequest(), true));
        }
        final List<Integer> bids = new ArrayList<>(playerCount);
        final Set<Capital> options = new HashSet<>(playerCount);
        asyncBids.stream().map(FutureOrDefault::get).mapToInt(IntegerResponse::getInt).forEach(bids::add);
        players.clear();
        while (!selectionOrder.isEmpty()) {
            int highestBid = -1;
            int index = -1;
            for (int i = 0; i < bids.size(); ++i) {
                if (bids.get(i) > highestBid) {
                    index = i;
                    highestBid = bids.get(i);
                }
            }
            bids.remove(index);
            final Player player = selectionOrder.remove(index);
            options.add(Capital.values()[players.size()]);
            players.add(player);
            turnOrder.add(player);
            log(player + " bids " + highestBid + " for capital");
            player.adjustCash(-highestBid);
        }
        for (Player player : turnOrder) {
            final Capital capital;
            if (options.size() > 1) {
                capital = new FutureOrDefault<>(player, new SelectCapitalRequest("Select Power to play", getGameState(), options), false).get().getCapital();
                log(player + " picks " + capital);
            } else {
                capital = options.iterator().next();
                log(player + " takes " + capital);
            }
            player.notify(new CapitalNotification(capital));
            player.selectCapital(capital);
            Node.nodeMap.values().stream().filter(n -> n.getCapital() == capital).findAny().ifPresent(player::addCity);
            options.remove(capital);
        }
        phase = Phase.ORDER_OF_PLAY;
    }

    private void orderOfPlayPhase() {
        final GameState gameState = getGameState();
        turnOrder.clear();
        final List<FutureOrDefault<BidForTurnOrderRequest, IntegerResponse>> asyncBids = new ArrayList<>(playerCount);
        for (Player player : players) {
            asyncBids.add(new FutureOrDefault<>(player, new BidForTurnOrderRequest(gameState, player.getCash()), true));
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
            log(player + " purchases " + lowestBid + " tokens");
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
                final CommodityResponse commodityReponse = new FutureOrDefault<>(player, new AdjustShortageSurplusRequest("Pay off shortage/surplus?", getGameState(), options), false).get();
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
            final CommodityResponse commodityReponse = new FutureOrDefault<>(player, new AdjustShortageSurplusRequest("Adjust shortage/surplus?", getGameState(), options), false).get();
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
                log(player + " draws a card");
                player.cards.add(c);
                player.notify(new CardNotification(c));
            }
        }
        phase = Phase.BUY_CARD;
    }

    private void buyCardPhase() {
        queryForRenaissance();
        for (Player player : turnOrder) {
            if (player.getCash() >= 10 && deck.isEmpty() && player.getAdvances().contains(Advance.urbanAscendancy)) {
                if (new FutureOrDefault<>(player, new UseUrbanAscendancyRequest(getGameState()), false).get().getBool()) {
                    final Card c = drawCard();
                    if (c != null) {
                        log(player + " buys a card");
                        player.adjustCash(-10);
                        player.cards.add(c);
                        player.notify(new CardNotification(c));
                    }
                }
            }
        }
        final List<FutureOrDefault<SelectCardRequest, IntegerResponse>> masterArtResponses = new ArrayList<>();
        for (Player player : turnOrder) {
            if (!player.cards.isEmpty() && player.getAdvances().contains(Advance.masterArt)) {
                masterArtResponses.add(new FutureOrDefault<>(player, new SelectCardRequest("Discard 1 card with Master Art?", getGameState(), player.cards, true), false));
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
                final int cardIndex = new FutureOrDefault<>(player, new SelectCardRequest("Play 1 card?", getGameState(), playableCards, true), false).get().getInt();
                if (cardIndex == -1) {
                    log(player + " passes");
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
        final List<FutureOrDefault<UpgradeShipsRequest, BooleanResponse>> asyncShipUpgrades = new ArrayList<>(playerCount);
        GameState gameState = getGameState();
        for (Player player : turnOrder) {
            if (player.shipLevel < 4 && player.getCash() >= 10) {
                asyncShipUpgrades.add(new FutureOrDefault<>(player, new UpgradeShipsRequest(gameState), true));
            } else {
                asyncShipUpgrades.add(null);
            }
        }
        for (int i = 0; i < playerCount; ++i) {
            final FutureOrDefault<UpgradeShipsRequest, BooleanResponse> response = asyncShipUpgrades.get(i);
            if (response != null && response.get().getBool()) {
                turnOrder.get(i).adjustCash(-10);
                ++turnOrder.get(i).shipLevel;
                log(turnOrder.get(i) + " upgrades ships to level " + turnOrder.get(i).shipLevel);
            }
        }

        for (int i = 0; i < playerCount; ++i) {
            final Player player = turnOrder.get(i);
            final List<Advance> advances = new FutureOrDefault<>(player, new PurchaseAdvancesRequest(getGameState(), players.indexOf(player)), false).get().getAdvances();
            for (Advance advance : advances) {
                player.research(advance);
            }
        }

        final Map<Player, FutureOrDefault<StabilizationRequest, BooleanResponse>> asyncDecisions = new HashMap<>(playerCount);
        gameState = getGameState();
        for (Player player : players) {
            final int cost = player.cards.size() * (player.cards.size() + 1) / 2;
            if (cost > 0 && player.getCash() >= cost) {
                asyncDecisions.put(player, new FutureOrDefault<>(player, new StabilizationRequest(gameState), true));
            }
        }
        for (Player player : players) {
            int cost = player.cards.size() * (player.cards.size() + 1) / 2;
            if (cost > 0) {
                final FutureOrDefault<StabilizationRequest, BooleanResponse> response = asyncDecisions.get(player);
                if (response != null && response.get().getBool()) {
                    log(player + " pays " + cost + " for stabilization");
                    player.adjustCash(-cost);
                } else {
                    log (player + " pays stabilization with misery");
                    while (!player.chaos && cost > 0) {
                        player.adjustMisery(1);
                        cost -= Player.miserySteps[player.misery] - Player.miserySteps[player.misery - 1];
                    }
                }
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

        final int indulgenceOwners = (int) players.stream().filter(p -> p.getAdvances().contains(Advance.holyIndulgence)).count();
        for (Player player : turnOrder) {
            if (player.getAdvances().contains(Advance.holyIndulgence)) {
                final int maxExtra = (playerCount - indulgenceOwners) * 2;
                final int extra = Math.min(maxExtra, player.getRemainingTokens());
                player.moveTokens(extra);
                if (extra < maxExtra) player.adjustCash(maxExtra - extra);
            } else {
                final int maxPayment = indulgenceOwners * 2;
                final int payment = Math.min(maxPayment, player.getUsableTokens());
                player.moveTokens(-payment);
                if (payment < maxPayment) {
                    boolean payCash = false;
                    if (player.getCash() >= maxPayment - payment) {
                        payCash = new FutureOrDefault<>(player, new SelectHolyIndulgencePaymentRequest(getGameState()), false).get().getBool();
                    }
                    if (payCash) player.adjustCash(payment - maxPayment);
                    else player.adjustMisery(1);
                }
            }
        }

        int cardCost = 3;
        for (int i = 0; i < playerCount; ++i) {
            boolean cardAvailable = !deck.isEmpty();
            final Player player = turnOrder.get(i);
            final Map<Node, Integer> usedShipping = new HashMap<>();
            final int groundRange = player.getAdvances().contains(Advance.caravan) ? 2 : 1;
            final boolean useHeavens = player.getAdvances().contains(Advance.heavens);
            final int shipRange = player.getAdvances().contains(Advance.seaworthyVessels) ? Integer.MAX_VALUE : player.shipLevel * 2;
            final int shipCapacity = player.getAdvances().contains(Advance.oceanNavigation) ? Integer.MAX_VALUE : player.shipLevel * 2 + (player.getAdvances().contains(Advance.seaworthyVessels) ? 8 : 0);

            while (player.getUsableTokens() > 0) {
                final Set<Node> reachableLimited = new HashSet<>();
                final Set<Node> reachableUnlimited = new HashSet<>();
                final Set<Node> fullAreas = new HashSet<>();
                players.forEach(p -> p.getCities().forEach(fullAreas::add));

                final int turnOrderRollRequirement = getTurnOrderThreshold(i, playerCount);
                if (shipRange == Integer.MAX_VALUE) {
                    if (shipCapacity == Integer.MAX_VALUE)
                        Node.nodeMap.values().stream().filter(Node::isCoastal).forEach(reachableUnlimited::add);
                    else Node.nodeMap.values().stream().filter(Node::isCoastal).forEach(reachableLimited::add);
                }
                final Set<Node> areas = new HashSet<>();
                player.getAreas().forEach(areas::add);
                Node.nodeMap.values().stream().filter(n -> n.getCapital() == player.getCapital()).findAny().ifPresent(areas::add);
                areas.forEach(node -> {
                    if (!fullAreas.contains(node)) reachableUnlimited.add(node);
                    reachableUnlimited.addAll(node.getReachableNodes(groundRange, false, false, fullAreas, playerCount));
                    if (shipRange != Integer.MAX_VALUE) {
                        reachableLimited.addAll(node.getReachableNodes(shipRange, true, useHeavens, Collections.emptySet(), playerCount));
                    }
                });
                final Map<Node, Integer> capacityMap = new HashMap<>();
                reachableLimited.forEach(n -> {
                    capacityMap.put(n, shipCapacity - usedShipping.getOrDefault(n, 0));
                });
                final ExpansionResponse response = new FutureOrDefault<>(player, new ExpansionRequest(getGameState(), players.indexOf(player), player.getUsableTokens(), reachableUnlimited, capacityMap, cardAvailable ? cardCost : Integer.MAX_VALUE), false).get();
                if (response.getTokensDisbanded() > 0) {
                    player.moveTokens(-response.getTokensDisbanded());
                }
                if (response.isCardPurchased()) {
                    final Card c = drawCard();
                    if (c != null) {
                        player.moveTokens(-cardCost);
                        cardAvailable = false;
                        log(player + " buys a card for " + cardCost);
                        cardCost += 3;
                        player.cards.add(c);
                        player.notify(new CardNotification(c));
                    }
                }
                response.getEntryStream().forEach(e -> {
                    final Node node = Node.nodeMap.get(e.getKey());
                    final int tokens = e.getValue();
                    if (node != null) {
                        if (!reachableUnlimited.contains(node) && reachableLimited.contains(node)) {
                            final int oldTokens = usedShipping.getOrDefault(node, 0);
                            usedShipping.put(node, oldTokens + tokens);
                        }
                        final int myExistingTokens = player.getTokenCount(node);
                        final int existingTokens = players.stream().map(p -> p.getTokenCount(node)).mapToInt(Integer::intValue).sum();
                        if (existingTokens == 0 && node.getSize() == 1) {
                            player.addNewTokens(node, tokens);
                            player.spendTokens(tokens);
                            log(player + " tokenizes " + node.getName());
                        } else if (myExistingTokens == existingTokens && existingTokens + tokens == node.getSize()) {
                            player.removeAllTokens(node);
                            player.addNewCity(node);
                            player.moveTokens(-tokens);
                            log(player + " occupies " + node.getName());
                        } else if (existingTokens + tokens >= node.getSize()) {
                            log (player + " attacks " + node.getName());
                            final boolean cathedral = player.getAdvances().contains(Advance.cathedral);
                            boolean autoLoss = false;
                            if (!cathedral) {
                                for (Player p : turnOrder) {
                                    if (p == player) continue;

                                    final int idx = players.indexOf(player);
                                    if (p.getAdvances().contains(Advance.cathedral) && round > p.cathedralUsed[idx] && p.getTokenCount(node) > 0) {
                                        final boolean cathedralUsed = new FutureOrDefault<>(player, new UseCathedralRequest(getGameState(), node.getName()), false).get().getBool();
                                        if (cathedralUsed) {
                                            log(p + " uses Cathedral to defend");
                                            p.cathedralUsed[idx] = round;
                                            autoLoss = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            final boolean proselytism = player.getAdvances().contains(Advance.proselytism);
                            final boolean win;
                            if (autoLoss) {
                                win = false;
                            } else if (proselytism && turnOrderRollRequirement == 1) {
                                win = true;
                                log(player + " uses Proselytism to win");
                            } else {
                                if (cathedral && e.getKey().equals(response.getCathedralused())) {
                                    for (Player p : turnOrder) {
                                        if (p == player) continue;

                                        if (p.getTokenCount(node) > 0) {
                                            final int idx = players.indexOf(p);
                                            player.cathedralUsed[idx] = round;
                                        }
                                    }
                                    win = true;
                                    log(player + " uses Cathedral to win " + node.getName());
                                } else {
                                    final int turnOrderRoll = r.nextInt(6);
                                    if (turnOrderRoll > turnOrderRollRequirement || turnOrderRoll == turnOrderRollRequirement && proselytism) {
                                        win = true;
                                        log("Turn order roll: " + (turnOrderRoll + 1) + " - win!");
                                    } else {
                                        log("Turn order roll: " + (turnOrderRoll + 1));
                                        final int attackerRoll = r.nextInt(6);
                                        final int defenderRoll = r.nextInt(6);
                                        log("Attacker: " + (attackerRoll + 1) + " Defender: " + (defenderRoll + 1));
                                        if (attackerRoll > defenderRoll) {
                                            win = true;
                                        } else if (attackerRoll == defenderRoll) {
                                            win = players.stream().noneMatch(p -> p != player && p.getTokenCount(node) > 0 && getAttackModifier(player.weapons, p.weapons) <= 0);
                                        } else {
                                            win = false;
                                        }
                                        if (win) {
                                            log("Attacker wins");
                                        } else {
                                            log("Defender wins");
                                        }
                                    }
                                }
                            }
                            if (win) {
                                for (Player p : players) p.removeAllTokens(node);
                                player.addNewCity(node);
                            } else {
                                player.removeAllTokens(node);
                            }
                            player.moveTokens(-tokens);
                        } else {
                            player.addNewTokens(node, tokens);
                            player.spendTokens(tokens);
                            log(player + " tokenizes " + node.getName());
                        }
                    }
                });
                //player.spendTokens(response.getTokensUsed());
            }
        }
        phase = Phase.INCOME;
    }

    public static int getAttackModifier(Collection<Integer> attackerWeapons, Collection<Integer> defenderWeapons) {
        final int attackerBestWeapon = attackerWeapons.stream().mapToInt(Integer::intValue).max().orElse(0);
        final int defenderBestWeapon = defenderWeapons.stream().mapToInt(Integer::intValue).max().orElse(0);
        final int attackerModifier = attackerBestWeapon > defenderBestWeapon ? (int) attackerWeapons.stream().filter(w -> w > defenderBestWeapon).count() : 0;
        final int defenderModifier = defenderBestWeapon > attackerBestWeapon ? (int) defenderWeapons.stream().filter(w -> w > attackerBestWeapon).count() : 0;
        if (attackerModifier > 0) return attackerModifier;
        else return -defenderModifier;
    }

    public static int getTurnOrderThreshold(int i, int playerCount) {
        final int[][] tables = {{2, 4, 6}, {1, 3, 5, 6}, {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5, 6}};
        return tables[playerCount - 3][i];
    }

    private void incomePhase() {
        int mostNewCities = 0;
        Player winningPlayer = null;
        for (Player player : players) {
            player.weapons.clear();
            if (player.getAdvances().contains(Advance.enlightenment)) {
                player.adjustMisery(-1);
            }
            final int newCities = player.getNewCityCount();
            log(player + " has " + newCities + " new cities");
            if (newCities > mostNewCities) {
                mostNewCities = newCities;
                winningPlayer = player;
            }
            player.flipTokens();
            player.adjustCash(player.getIncome(playerCount));
        }
        if (winningPlayer != null) {
            final Card c = drawCard();
            if (c != null) {
                log(winningPlayer + " gets a card");
                winningPlayer.cards.add(c);
                winningPlayer.notify(new CardNotification(c));
            }
        }
        shortages.clear();
        surpluses.clear();
        for (int i = 0; i < 2; ++i) {
            final int roll1 = r.nextInt(6) + 1;
            final int roll2 = r.nextInt(6) + 1;
            final boolean shortage = r.nextBoolean();
            for (Commodity commodity : Commodity.values()) {
                final int requireedSum = Math.min(commodity.ordinal() + 2, 12);
                if (roll1 + roll2 == requireedSum) {
                    if (shortage) shortages.add(commodity);
                    else surpluses.add(commodity);
                    final int max = players.stream().map(p -> p.getCommodityCount(commodity)).mapToInt(Integer::intValue).max().orElse(0);
                    if (max > 0) {
                        final List<Player> targets = players.stream().filter(p -> p.getCommodityCount(commodity) == max).toList();
                        if (targets.size() == 1) {
                            final Player player = targets.get(0);
                            if (shortage) {
                                log(commodity + " shortage");
                                final Card c = drawCard();
                                if (c != null) {
                                    log(player + " draws a card");
                                    player.cards.add(c);
                                    player.notify(new CardNotification(c));
                                }
                            } else {
                                final int payment = Math.min(player.getCash(), max);
                                log(commodity + " surplus");
                                log(player + " loses " + payment + " cash");
                                player.adjustCash(-payment);
                            }
                        }
                    }
                }
            }
        }
        while (!surpluses.isEmpty() || !shortages.isEmpty()) {
            final Player player = turnOrder.get(0);
            final Set<Commodity> options = new HashSet<>();
            for (Commodity commodity : surpluses) if (player.getCash() >= commodity.getValue()) options.add(commodity);
            for (Commodity commodity : shortages) if (player.getCash() >= commodity.getValue()) options.add(commodity);
            if (!options.isEmpty()) {
                final CommodityResponse commodityReponse = new FutureOrDefault<>(player, new AdjustShortageSurplusRequest("Pay off shortage/surplus?", getGameState(), options), false).get();
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
                final int cardIndex = new FutureOrDefault<>(player, new SelectCardRequest("Play 1 card", getGameState(), player.cards, false), false).get().getInt();
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
                final int delta = new FutureOrDefault<>(player, new UseRenaissanceRequest(getGameState(), renaissanceOptions), false).get().getInt();
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
                log((war1.chaos ? war1 : war2) + " is in chaos, war ends");
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
                    final int index = new FutureOrDefault<>(currentPlayer, new SelectCardRequest("Play weapons for war?", getGameState(), playableWeapons, true), false).get().getInt();
                    if (index == -1) {
                        break;
                    } else {
                        final WeaponCard card = playableWeapons.remove(index);
                        currentPlayer.cards.remove(card);
                        playableWeapons.remove(index);
                        card.play(this, currentPlayer);
                        bestWeapon1 = war1.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
                        bestWeapon2 = war2.weapons.stream().mapToInt(Integer::intValue).max().orElse(0);
                    }
                }
            }
            final int best1 = bestWeapon1;
            final int best2 = bestWeapon2;
            final int mod1 = bestWeapon1 > bestWeapon2 ? (int) war1.weapons.stream().filter(w -> w > best2).count() : 0;
            final int mod2 = bestWeapon2 > bestWeapon1 ? (int) war2.weapons.stream().filter(w -> w > best1).count() : 0;
            final int r1 = r.nextInt(6);
            final int r2 = r.nextInt(6);
            final int roll1 = r1 + mod1;
            final int roll2 = r2 + mod2;
            log(war1 + " rolls " + (r1 + 1) + (mod1 > 0 ? (" + " + mod1) : ""));
            log(war2 + " rolls " + (r2 + 1) + (mod2 > 0 ? (" + " + mod1) : ""));
            if (roll1 != roll2) {
                final Player winner =  roll1 > roll2 ? war1 : war2;
                final Player loser = roll1 > roll2 ? war2 : war1;
                final int asiaLimit = (int) Math.max(0, winner.shipLevel - winner.getAreas().filter(Node::isInAsia).count());
                final int newWorldLimit = (int) Math.max(0, winner.shipLevel - winner.getAreas().filter(Node::isInNewWorld).count());
                final Set<String> options = loser.getCities()
                        .filter(n -> n.isAccessible(winner.getAdvances()))
                        .filter(n -> !n.isInAsia() || asiaLimit > 0)
                        .filter(n -> !n.isInNewWorld() || newWorldLimit > 0)
                        .map(Node::getName).collect(Collectors.toSet());
                final int count = Math.abs(roll1 - roll2);
                if (!options.isEmpty()) {
                    final List<String> targets;
                    if (options.size() > count) {
                        log(loser + " loses " +  count + " cities");
                        targets = new FutureOrDefault<>(loser, new SelectTargetCitiesRequest("Choose cities to lose in War!", getGameState(), options, count, false, asiaLimit, newWorldLimit), false).get().getCities();
                    } else {
                        targets = new ArrayList<>(options);
                    }
                    for (String target : targets) {
                        log(loser + " loses " + target);
                        final List<Node> lostCities = loser.getCities().filter(c -> c.getName().equals(target)).toList();
                        lostCities.forEach(n -> {
                            loser.remove(n);
                            winner.addCity(n);
                        });
                    }
                } else {
                    log(loser + " has no suitable cities");
                }
                loser.adjustMisery(1 + Math.max(0, count - options.size()));
                war1 = null;
                war2 = null;
            } else if (mod1 != mod2) {
                if (mod1 > mod2) {
                    log(war1 + " wins with tiebreak");
                    war2.adjustMisery(1);
                }
                else {
                    log(war2 + " wins with tiebreak");
                    war1.adjustMisery(1);
                }
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
        state.playedCards = playedCards.stream().filter(c -> !(c instanceof LeaderCard)).mapToInt(Card::getIndex).toArray();
        players.forEach(p -> state.players.add(p.getState()));
        state.turnOrder = new int[players.size()];
        state.war1 = war1 == null ? -1 : players.indexOf(war1);
        state.war2 = war2 == null ? -1 : players.indexOf(war2);
        for (int i = 0; i < turnOrder.size(); ++i) {
            state.turnOrder[i] = players.indexOf(turnOrder.get(i));
        }
        state.patronageCards = new int[patronageQueue.size()];
        state.patronageUsesRemaining = new int[patronageQueue.size()];
        state.patronageOwners = new int[patronageQueue.size()];
        for (int i = 0; i < patronageQueue.size(); ++i) {
            state.patronageCards[i] = patronageQueue.get(i).getIndex();
            state.patronageUsesRemaining[i] = patronageQueue.get(i).usesRemaining;
            state.patronageOwners[i] = players.indexOf(patronageQueue.get(i).owner);
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
        boolean mysticismPlayable = false;
        boolean alchemistsGoldPlayable = false;
        for (Player player : players) {
            player.purchasePhaseFinished();
            if (player.getAdvances().stream().map(Advance::getCategory).filter(c -> c == Advance.Category.SCIENCE).count() < 4) {
                mysticismPlayable = true;
            }
            if (!player.getAdvances().contains(Advance.lawsOfMatter)) {
                alchemistsGoldPlayable = true;
            }
        }
        if (!mysticismPlayable) unplayableCards.add(Cards.mysticismAbounds);
        if (!alchemistsGoldPlayable) unplayableCards.add(Cards.alchemistsGold);

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
        if (surpluses.remove(commodity)) {
            log(commodity + " surplus used");
            --adjustment;
        }
        if (shortages.remove(commodity)) {
            log(commodity + " shortage used");
            ++adjustment;
        }
        for (Player player : players) {
            int count = player.getCommodityCount(commodity);
            if (count > 0) {
                if (player.getAdvances().contains(Advance.industry)) ++count;
                count += adjustment;
                final int value = count * count * commodity.getValue();
                log(player + " got " + value + " cash from " + commodity);
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

    public void log(String s) {
        for (Player player : players) {
            player.notify(new LogEntryNotification(s));
        }
        System.err.println(s);
    }
}
