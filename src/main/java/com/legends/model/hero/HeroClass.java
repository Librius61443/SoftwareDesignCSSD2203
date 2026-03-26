package com.legends.model.hero;
//Base classes
public enum HeroClass {
 
    ORDER("Order") {
        @Override public int bonusManaPerLevel()    { return 5; }
        @Override public int bonusDefensePerLevel() { return 2; }
    },
    CHAOS("Chaos") {
        @Override public int bonusAttackPerLevel()  { return 3; }
        @Override public int bonusHpPerLevel()      { return 5; }
    },
    WARRIOR("Warrior") {
        @Override public int bonusAttackPerLevel()  { return 2; }
        @Override public int bonusDefensePerLevel() { return 3; }
    },
    MAGE("Mage") {
        @Override public int bonusManaPerLevel()    { return 5; }
        @Override public int bonusAttackPerLevel()  { return 1; }
    };
 
    private final String displayName;
 
    HeroClass(String displayName) {
        this.displayName = displayName;
    }
 
    public String getDisplayName() { return displayName; }
 
    // Default bonuses are 0; subclasses override only what is needed
    public int bonusAttackPerLevel()  { return 0; }
    public int bonusDefensePerLevel() { return 0; }
    public int bonusHpPerLevel()      { return 0; }
    public int bonusManaPerLevel()    { return 0; }
 
    @Override
    public String toString() { return displayName; }
}
