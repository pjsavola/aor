package aor;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class TestServer extends Server {

    public Deque<Integer> predefinedRandomNumbers = new ArrayDeque();

    public TestServer() {
        super(null);
    }

    @Override
    protected void initDecks() {
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
        epoch1.add(Cards.theCrusades);
        epoch1.add(Cards.walterThePenniless);
        epoch1.add(Cards.rashidAdDin);
        epoch1.add(Cards.silk1);
        epoch1.add(Cards.spice1);

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
    protected int getRandomNumber(int bound) {
        return predefinedRandomNumbers.isEmpty() ? super.getRandomNumber(bound) : Math.min(bound - 1, predefinedRandomNumbers.pop());
    }

    @Override
    protected void shuffle(List<?> list) {
        if (predefinedRandomNumbers.isEmpty()) {
            super.shuffle(list);
        }
    }
}
