package aor;

public class WeaponCard extends Card {

    private final int power;

    public WeaponCard(String name, int power) {
        super(name, true);
        this.power = power;
    }

    @Override
    public void play(Game game, Player player) {
        super.play(game, player);
    }
}
