package unit;

public enum HeroClass {
  ORDER("Order", 0, 2, 0, 5),
  CHAOS("Chaos", 3, 0, 5, 0),
  WARRIOR("Warrior", 2, 3, 0, 0),
  MAGE("Mage", 1, 0, 0, 5);

  private final String displayName;
  private final int attackBonusPerLevel;
  private final int defenseBonusPerLevel;
  private final int hpBonusPerLevel;
  private final int manaBonusPerLevel;

  HeroClass(String displayName, int attackBonusPerLevel, int defenseBonusPerLevel,
            int hpBonusPerLevel, int manaBonusPerLevel) {
    this.displayName = displayName;
    this.attackBonusPerLevel = attackBonusPerLevel;
    this.defenseBonusPerLevel = defenseBonusPerLevel;
    this.hpBonusPerLevel = hpBonusPerLevel;
    this.manaBonusPerLevel = manaBonusPerLevel;
  }

  public String displayName() { return displayName; }
  public int attackBonusPerLevel() { return attackBonusPerLevel; }
  public int defenseBonusPerLevel() { return defenseBonusPerLevel; }
  public int hpBonusPerLevel() { return hpBonusPerLevel; }
  public int manaBonusPerLevel() { return manaBonusPerLevel; }
}
