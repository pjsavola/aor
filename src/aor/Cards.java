package aor;

public abstract class Cards {
    public static final Card stirrups = new WeaponCard("Stirrups", 1);
    public static final Card armor = new WeaponCard("Armor", 2);
    public static final Card longBow = new WeaponCard("Long Bow", 3).invalidates(stirrups, armor);
    public static final Card gunpowder = new WeaponCard("Gunpowder", 4).invalidates(stirrups, armor);

    public static final Card alchemistsGold = new EventCard(EventCard.Type.ALCHEMISTS_GOLD);
    public static final Card civilWar = new EventCard(EventCard.Type.CIVIL_WAR);
    public static final Card enlightenedRuler = new EventCard(EventCard.Type.ENLIGHTENED_RULER);
    public static final Card famine = new EventCard(EventCard.Type.FAMINE);
    public static final Card mysticismAbounds = new EventCard(EventCard.Type.MYSTICISM_ABOUNDS);
    public static final Card papalDecree = new EventCard(EventCard.Type.PAPAL_DECREE);
    public static final Card piratesVikings = new EventCard(EventCard.Type.PIRATES_VIKINGS);
    public static final Card rebellion = new EventCard(EventCard.Type.REBELLION);
    public static final Card revolutionaryUprisings = new EventCard(EventCard.Type.REVOLUTIONARY_UPRISINGS);
    public static final Card theCrusades = new EventCard(EventCard.Type.THE_CRUSADES);
    public static final Card war = new EventCard(EventCard.Type.WAR);
    public static final Card blackDeath = new EventCard(EventCard.Type.BLACK_DEATH);
    public static final Card religiousStrife = new EventCard(EventCard.Type.RELIGIOUS_STRIFE).invalidates(papalDecree);
    public static final Card mongolArmies = new EventCard(EventCard.Type.MONGOL_ARMIES).invalidates(theCrusades);

    public static final Card charlemagne = new LeaderCard("Charlemagne", 20, Advance.nationalism);
    public static final Card dionysusExiguus = new LeaderCard("Dionysus Exiguus", 20, Advance.writtenRecord);
    public static final Card stBenedict = new LeaderCard("St. Benedict", 10, Advance.writtenRecord, Advance.patronage);
    public static final Card rashidAdDin = new LeaderCard("Rashid ad Din", 10, Advance.writtenRecord, Advance.overlandEast);
    public static final Card walterThePenniless = new LeaderCard("Walter the Penniless", 20, theCrusades, 10, Advance.overlandEast);

    public static final Card christopherColumbus = new LeaderCard("Christopher Columbus", 30, Advance.oceanNavigation, Advance.newWorld);
    public static final Card desideriusErasmus = new LeaderCard("Desiderius Erasmus", 20, Advance.printedWord, Advance.renaissance);
    public static final Card ibnMajid = new LeaderCard("Ibn Majid", 20, Advance.oceanNavigation, Advance.cosmopolitan);
    public static final Card johannGutenberg = new LeaderCard("Johann Gutenberg", 30, Advance.printedWord);
    public static final Card marcoPolo = new LeaderCard("Marco Polo", 20, mongolArmies, 20, Advance.overlandEast, Advance.cosmopolitan);
    public static final Card nicolausCopernicus = new LeaderCard("Nicolaus Copernicus", 20, Advance.heavens, Advance.institutionalResearch);
    public static final Card princeHenry = new LeaderCard("Prince Henry", 20, Advance.oceanNavigation, Advance.institutionalResearch);
    public static final Card williamCaxton = new LeaderCard("William Caxton", 20, Advance.printedWord);


    public static final Card andreasVesalius = new LeaderCard("Andreas Vesalius", 20, Advance.humanBody, Advance.enlightenment);
    public static final Card bartolomeDeLasCasas = new LeaderCard("Bartolome de Las Casas", 30, Advance.cosmopolitan);
    public static final Card galileoGalilei = new LeaderCard("Galileo Galilei", 20, Advance.heavens, Advance.renaissance);
    public static final Card henryOldenburg = new LeaderCard("Henry Oldenburg", 30, Advance.enlightenment);
    public static final Card leonardoDaVinci = new LeaderCard("Leonardo da Vinci", 20, Advance.masterArt, Advance.humanBody, Advance.renaissance);
    public static final Card sirIsaacNewton = new LeaderCard("Sir Isaac Newton", 20, Advance.lawsOfMatter, Advance.enlightenment);

    public static final Card stone1 = new CommodityCard(Commodity.STONE);
    public static final Card stone2 = new CommodityCard(Commodity.STONE);
    public static final Card wool1 = new CommodityCard(Commodity.WOOL);
    public static final Card wool2 = new CommodityCard(Commodity.WOOL);
    public static final Card timber1 = new CommodityCard(Commodity.TIMBER);
    public static final Card timber2 = new CommodityCard(Commodity.TIMBER);
    public static final Card timber3 = new CommodityCard(Commodity.TIMBER);
    public static final Card grain1 = new CommodityCard(Commodity.GRAIN);
    public static final Card grain2 = new CommodityCard(Commodity.GRAIN);
    public static final Card cloth1 = new CommodityCard(Commodity.CLOTH);
    public static final Card cloth2 = new CommodityCard(Commodity.CLOTH);
    public static final Card clothWine = new DoubleCommodityCard(Commodity.CLOTH, Commodity.WINE);
    public static final Card wine1 = new CommodityCard(Commodity.WINE);
    public static final Card wine2 = new CommodityCard(Commodity.WINE);
    public static final Card metal1 = new CommodityCard(Commodity.METAL);
    public static final Card metal2 = new CommodityCard(Commodity.METAL);
    public static final Card metal3 = new CommodityCard(Commodity.METAL);
    public static final Card fur1 = new CommodityCard(Commodity.FUR);
    public static final Card fur2 = new CommodityCard(Commodity.FUR);
    public static final Card silk1 = new CommodityCard(Commodity.SILK);
    public static final Card silk2 = new CommodityCard(Commodity.SILK);
    public static final Card silk3 = new CommodityCard(Commodity.SILK);
    public static final Card spice1 = new CommodityCard(Commodity.SPICE);
    public static final Card spice2 = new CommodityCard(Commodity.SPICE);
    public static final Card spice3 = new CommodityCard(Commodity.SPICE);
    public static final Card goldIvory = new DoubleCommodityCard(Commodity.GOLD, Commodity.IVORY);
    public static final Card gold = new CommodityCard(Commodity.GOLD);
}
