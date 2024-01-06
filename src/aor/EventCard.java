package aor;

import message.*;

import java.util.*;
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

    public void play(Server game, Player player) {
        super.play(game, player);
        switch (type) {
            case ALCHEMISTS_GOLD -> {
                final Map<Capital, Player> targets = new HashMap<>();
                game.players.stream().filter(p -> p != game.enlightenedRuler && !p.getAdvances().contains(Advance.lawsOfMatter)).forEach(p -> targets.put(p.getCapital(), p));
                final Capital capital = new FutureOrDefault<>(player, new SelectCapitalRequest("Select target for Alchemist's Gold", game.getGameState(), targets.keySet()), false).get().getCapital();
                final Player target = targets.get(capital);
                final int loss = (target.writtenCash + 1) / 2;
                game.log(target + " lost " + loss + " cash");
                target.adjustCash(-loss);
            }
            case CIVIL_WAR -> {
                final Map<Capital, Player> targets = new HashMap<>();
                game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> targets.put(p.getCapital(), p));
                final Capital capital = new FutureOrDefault<>(player, new SelectCapitalRequest("Select target for Civil War", game.getGameState(), targets.keySet()), false).get().getCapital();
                final Player target = targets.get(capital);
                target.adjustMisery(1);
                game.civilWar = target;
                if (!target.chaos) {
                    final boolean loseTokens = new FutureOrDefault<>(target, new SelectCivilWarLossesRequest(game.getGameState()), false).get().getBool();
                    if (loseTokens) {
                        final int tokens = (player.getUsableTokens() + 1) / 2;
                        game.log(target + " loses " + tokens + " tokens");
                        player.moveTokens(-tokens);
                    } else {
                        final int cash = (player.writtenCash + 1) / 2;
                        game.log(target + " loses " + cash + " cash");
                        player.adjustCash(-cash);
                    }
                }
                for (Player p : game.players) p.getCities().filter(n -> n.getCapital() == capital).findAny().ifPresent(p::reduceCity);
            }
            case ENLIGHTENED_RULER -> game.enlightenedRuler = player;
            case FAMINE -> game.players.forEach(p -> p.adjustMisery(Math.max(0, (p.getAdvances().contains(Advance.improvedAgriculture) ? 3 : 4) - p.getCommodityCount(Commodity.GRAIN))));
            case MYSTICISM_ABOUNDS -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery(4 - (int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.SCIENCE).count()));
            case PAPAL_DECREE -> {
                final Set<Advance.Category> allowedCategories = Set.of(Advance.Category.SCIENCE, Advance.Category.RELIGION, Advance.Category.EXPLORATION);
                game.bannedCategory = new FutureOrDefault<>(player, new SelectCategoryRequest(game.getGameState(), allowedCategories), false).get().getCategory();
                if (game.bannedCategory != null) {
                    game.log("Pope bans " + game.bannedCategory);
                }
            }
            case PIRATES_VIKINGS -> {
                final Set<String> options = new HashSet<>();
                game.players.forEach(p -> p.getCities().filter(Node::isCoastal).map(Node::getName).forEach(options::add));
                if (!options.isEmpty()) {
                    final List<String> targets = new FutureOrDefault<>(player, new SelectTargetCitiesRequest("Choose targets for Pirates/Vikings", game.getGameState(), options, Math.min(Math.min(3, game.getEpoch()), options.size()), true, 3, true), false).get().getCities();
                    for (String target : targets) {
                        for (Player p : game.players) p.getCities().filter(n -> n.getName().equals(target)).findAny().ifPresent(p::reduceCity);
                    }
                } else {
                    game.log("No suitable targets!");
                }
            }
            case REBELLION -> {
                final Set<String> options = new HashSet<>();
                game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.getCities().filter(n -> n.getCapital() == null && !n.isInNewWorld()).map(Node::getName).forEach(options::add));
                if (!options.isEmpty()) {
                    final List<String> targets = new FutureOrDefault<>(player, new SelectTargetCitiesRequest("Choose target for Rebellion", game.getGameState(), options, 1, true, 1, false), false).get().getCities();
                    for (Player p : game.players) p.getCities().filter(n -> n.getName().equals(targets.get(0))).findAny().ifPresent(p::reduceCity);
                } else {
                    game.log("No suitable targets!");
                }
            }
            case REVOLUTIONARY_UPRISINGS -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.COMMERCE).count()));
            case THE_CRUSADES -> {
                final Set<String> options = Node.nodeMap.values().stream().filter(n -> n.getRegion() == 6 && player.getCities().noneMatch(c -> c == n)).map(Node::getName).collect(Collectors.toSet());
                if (!options.isEmpty()) {
                    final List<String> targets = new FutureOrDefault<>(player, new SelectTargetCitiesRequest("Choose target for Crusades", game.getGameState(), options, 1, false, 0, false), false).get().getCities();
                    final Node n = Node.nodeMap.get(targets.get(0));
                    if (n != null) {
                        game.log("Crusades at " + n.getName());
                        for (Player p : game.players) p.remove(n);
                        player.addCity(n);
                    }
                } else {
                    game.log("No suitable targets!");
                }
                player.adjustMisery(1);
            }
            case WAR -> {
                final Map<Capital, Player> targets = new HashMap<>();
                game.players.stream().filter(p -> p != player).forEach(p -> targets.put(p.getCapital(), p));
                if (!targets.isEmpty()) {
                    final Capital capital = new FutureOrDefault<>(player, new SelectCapitalRequest("Select target for War!", game.getGameState(), targets.keySet()), false).get().getCapital();
                    final Player target = targets.get(capital);
                    game.war1 = player;
                    game.war2 = target;
                    game.resolveWar(player);
                }
            }
            case BLACK_DEATH -> {
                final int area = new FutureOrDefault<>(player, new SelectAreaRequest(game.getGameState()), false).get().getInt();
                game.log("All tokens removed from area " + area);
                for (Player p : game.players) {
                    final List<Node> tokenAreas = p.getTokenAreas().filter(n -> n.getRegion() == area).toList();
                    for (Node n : tokenAreas) p.remove(n);
                    final List<Node> cityAreas = p.getCities().filter(n -> n.getRegion() == area).toList();
                    for (Node n : cityAreas) p.reduceCity(n);
                }
            }
            case RELIGIOUS_STRIFE -> game.players.stream().filter(p -> p != game.enlightenedRuler).forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.RELIGION).count()));
            case MONGOL_ARMIES -> player.adjustCash(10);
        }
    }
}
