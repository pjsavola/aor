package aor;

import message.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventCard extends Card {

    public enum Type {
        ALCHEMISTS_GOLD("Alchemist's Gold"),
        CIVIL_WAR("Civil War"),
        ENLIGHTENED_RULER("Enlightened Ruler"),
        FAMINE("Famine"),
        MYSTICISM_ABOUNDS("Mysticism Abounds"),
        PAPAL_DECREE("Papal Decree"),
        PIRATES_VIKINGS("Pirates / Vikings"),
        REBELLION("Rebellion"),
        REVOLUTIONARY_UPRISINGS("Revolutionary Uprisings"),
        THE_CRUSADES("The Crusades"),
        WAR("War"),
        BLACK_DEATH("Black Death"),
        RELIGIOUS_STRIFE("Religious Strife"),
        MONGOL_ARMIES("Mongol Armies");

        private final String name;

        private Type(String name) {
            this.name = name;
        }
    }

    private final Type type;

    public EventCard(Type type) {
        super(type.name, type == Type.MONGOL_ARMIES);
        this.type = type;
    }

    public void play(Game game, Player player) {
        super.play(game, player);
        switch (type) {
            case ALCHEMISTS_GOLD -> {
                final Map<Node.CityState, Player> targets = new HashMap<>();
                game.players.stream().filter(p -> p != game.enlightenedRuler && !p.getAdvances().contains(Advance.lawsOfMatter)).forEach(p -> targets.put(p.getCapital(), p));
                final Node.CityState capital = new FutureOrDefault<>(
                        player.send(new SelectCapitalRequest(game.getGameState())),
                        response -> targets.containsKey(response.getCapital()),
                        new CapitalResponse(targets.keySet().iterator().next())).getResult().getCapital();
                final Player target = targets.get(capital);
                target.adjustCash(-(target.writtenCash + 1) / 2);
            }
            case CIVIL_WAR -> {
                final Map<Node.CityState, Player> targets = new HashMap<>();
                game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> targets.put(p.getCapital(), p));
                final Node.CityState capital = new FutureOrDefault<>(
                        player.send(new SelectCapitalRequest(game.getGameState())),
                        response -> targets.containsKey(response.getCapital()),
                        new CapitalResponse(targets.keySet().iterator().next())).getResult().getCapital();
                final Player target = targets.get(capital);
                target.adjustMisery(1);
                game.civilWar = target;
                if (!target.chaos) {
                    final boolean loseTokens = new FutureOrDefault<>(
                            target.send(new SelectCivilWarLossesRequest(game.getGameState())),
                            response -> true,
                            new BooleanResponse(true)).getResult().getBool();
                    if (loseTokens) {
                        player.usableTokens -= (player.usableTokens + 1) / 2;
                    } else {
                        player.adjustCash(-(player.writtenCash + 1) / 2);
                    }
                }
                final Node node = game.nodes.stream().filter(n -> n.getCapital() == capital).findAny().orElse(null);
                for (Player p : game.players) {
                    final int idx = p.cities.indexOf(node);
                    if (idx != -1) {
                        p.reduce(p.cities.get(idx));
                        break;
                    }
                }
            }
            case ENLIGHTENED_RULER -> game.enlightenedRuler = player;
            case FAMINE -> game.players.forEach(p -> p.adjustMisery(Math.max(0, (p.getAdvances().contains(Advance.improvedAgriculture) ? 4 : 3) - p.getCommodityCount(Commodity.GRAIN))));
            case MYSTICISM_ABOUNDS -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery(4 - (int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.SCIENCE).count()));
            case PAPAL_DECREE -> {
                final Set<Advance.Category> allowedCategories = Set.of(Advance.Category.SCIENCE, Advance.Category.RELIGION, Advance.Category.EXPLORATION);
                game.bannedCategory = new FutureOrDefault<>(
                        player.send(new SelectCategoryRequest(game.getGameState())),
                        response -> response.getCategory() == null || allowedCategories.contains(response.getCategory()),
                        new CategoryResponse(null)).getResult().getCategory();
            }
            case PIRATES_VIKINGS -> {
                // Choose node(s)
            }
            case REBELLION -> {
                // Choose node(s)
            }
            case REVOLUTIONARY_UPRISINGS -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.COMMERCE).count()));
            case THE_CRUSADES -> {
                // Choose node(s)
            }
            case WAR -> {

            }
            case BLACK_DEATH -> {
                final int area = new FutureOrDefault<>(
                        player.send(new BidForTurnOrderRequest(game.getGameState())),
                        response -> response.getInt() >= 1 && response.getInt() <= 8,
                        new IntegerResponse(1)).getResult().getInt();
                for (Player p : game.players) {
                    p.reduce(game.nodes.stream().filter(n -> n.getRegion() == area).toList());
                }
            }
            case RELIGIOUS_STRIFE -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.RELIGION).count()));
            case MONGOL_ARMIES -> player.adjustCash(10);
        }
    }
}
