package aor;

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
            case ALCHEMISTS_GOLD -> {}
            case CIVIL_WAR -> {}
            case ENLIGHTENED_RULER -> {}
            case FAMINE -> game.players.forEach(p -> p.adjustMisery(Math.max(0, (p.getAdvances().contains(Advance.improvedAgriculture) ? 4 : 3) - p.getCommodityCount(Commodity.GRAIN))));
            case MYSTICISM_ABOUNDS -> game.players.forEach(p -> p.adjustMisery(4 - (int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.SCIENCE).count()));
            case PAPAL_DECREE -> {}
            case PIRATES_VIKINGS -> {}
            case REBELLION -> {}
            case REVOLUTIONARY_UPRISINGS -> game.players.forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.COMMERCE).count()));
            case THE_CRUSADES -> {}
            case WAR -> {}
            case BLACK_DEATH -> {}
            case RELIGIOUS_STRIFE -> game.players.forEach(p -> p.adjustMisery((int) p.getAdvances().stream().filter(a -> a.category == Advance.Category.RELIGION).count()));
            case MONGOL_ARMIES -> player.adjustCash(10);
        }
    }
}
