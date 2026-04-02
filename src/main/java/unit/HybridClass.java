package unit;

public enum HybridClass {
  PRIEST(HeroClass.ORDER, HeroClass.ORDER, "Priest"),
  HERETIC(HeroClass.ORDER, HeroClass.CHAOS, "Heretic"),
  PALADIN(HeroClass.ORDER, HeroClass.WARRIOR, "Paladin"),
  PROPHET(HeroClass.ORDER, HeroClass.MAGE, "Prophet"),
  INVOKER(HeroClass.CHAOS, HeroClass.CHAOS, "Invoker"),
  ROGUE(HeroClass.CHAOS, HeroClass.WARRIOR, "Rogue"),
  SORCERER(HeroClass.CHAOS, HeroClass.MAGE, "Sorcerer"),
  KNIGHT(HeroClass.WARRIOR, HeroClass.WARRIOR, "Knight"),
  WARLOCK(HeroClass.WARRIOR, HeroClass.MAGE, "Warlock"),
  WIZARD(HeroClass.MAGE, HeroClass.MAGE, "Wizard");

  private final HeroClass classA;
  private final HeroClass classB;
  private final String displayName;

  HybridClass(HeroClass classA, HeroClass classB, String displayName) {
    this.classA = classA;
    this.classB = classB;
    this.displayName = displayName;
  }

  public String displayName() { return displayName; }
  public int attackBonusPerLevel() { return classA.attackBonusPerLevel() + classB.attackBonusPerLevel(); }
  public int defenseBonusPerLevel() { return classA.defenseBonusPerLevel() + classB.defenseBonusPerLevel(); }
  public int hpBonusPerLevel() { return classA.hpBonusPerLevel() + classB.hpBonusPerLevel(); }
  public int manaBonusPerLevel() { return classA.manaBonusPerLevel() + classB.manaBonusPerLevel(); }

  public static HybridClass of(HeroClass a, HeroClass b) {
    for (HybridClass hybrid : values()) {
      if ((hybrid.classA == a && hybrid.classB == b) || (hybrid.classA == b && hybrid.classB == a)) {
        return hybrid;
      }
    }
    return null;
  }
}
