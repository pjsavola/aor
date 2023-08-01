package message;

import aor.*;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class PurchaseAdvancesRequest extends Request<PurchaseAdvancesResponse> {
    public final int playerIndex;

    public PurchaseAdvancesRequest(GameState gameState, int playerIndex) {
        super("Purchase Advances", gameState);
        this.playerIndex = playerIndex;
    }

    @Override
    public boolean validateResponse(PurchaseAdvancesResponse response) {
        final PlayerState playerState = gameState.turnOrder.get(playerIndex);
        int cash = playerState.cash;
        int misery = playerState.misery;
        final Set<Advance> advances = Arrays.stream(playerState.advances).mapToObj(i -> Advance.allAdvances.get(i)).collect(Collectors.toSet());
        final Set<Advance> allAdvances = new HashSet<>(advances);
        final Set<Card> playedCards = Arrays.stream(gameState.playedCards).mapToObj(i -> Card.allCards.get(i)).collect(Collectors.toSet());
        final LeaderCard[] patronageQueue = Arrays.stream(gameState.patronageCards).mapToObj(i -> (LeaderCard) Card.allCards.get(i)).toArray(LeaderCard[]::new);
        final Set<Card> ownedPatronageCards = playerState.ownedPatronageCards.stream().map(i -> Card.allCards.get(i)).collect(Collectors.toSet());
        final List<Advance> list = response.getAdvances();
        for (Advance advance : list) {
            if (misery >= Player.miserySteps.length) {
                return false;
            }
            if (advance.getCategory() == gameState.bannedCategory && !playedCards.contains(Cards.religiousStrife)) {
                return false;
            }
            if (!allAdvances.containsAll(advance.getPrerequisites())) {
                return false;
            }
            final int cost = advance.getCost(advances);
            int maxDiscount = 0;
            LeaderCard bestCard = null;
            for (int i = 0; i < patronageQueue.length; ++i) {
                final LeaderCard card = patronageQueue[i];
                for (Advance discount : card.getAdvances()) {
                    if (advance == discount) {
                        final int amount = card.getAmount(playedCards);
                        if (amount > maxDiscount) {
                            if (ownedPatronageCards.contains(card) || (gameState.patronageUsesRemaining[i] > 0 && allAdvances.contains(Advance.patronage))) {
                                maxDiscount = amount;
                                bestCard = card;
                            }
                        }
                    }
                }
            }
            final int discount = bestCard == null ? 0 : bestCard.getAmount(playedCards);
            final int finalCost = Math.max(0, cost - discount);
            if (cash < finalCost) {
                return false;
            }
            cash -= finalCost;
            allAdvances.add(advance);
            if (advance == Advance.humanBody || advance == Advance.improvedAgriculture) {
                misery = Math.max(0, misery - 1);
            }
            if (advance.getCategory() == Advance.Category.RELIGION) {
                ++misery;
            }
        }
        return true;
    }

    @Override
    public PurchaseAdvancesResponse getDefaultResponse() {
        return new PurchaseAdvancesResponse(Collections.emptyList());
    }
}