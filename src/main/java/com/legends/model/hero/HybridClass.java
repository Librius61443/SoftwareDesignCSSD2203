package com.legends.model.hero;

/**
 * Hybrid classes hero reaches level 5 in two different base classes.
 * Lookup: HybridClass.of(classA, classB) — order-independent.
 */
public enum HybridClass {
    // Order 
    PRIEST(HeroClass.ORDER, HeroClass.ORDER,
            "Priest",
            "Heal now applies to ALL friendly units instead of just the lowest-HP hero."),

    HERETIC(HeroClass.ORDER, HeroClass.CHAOS,
            "Heretic",
            "Gain access to both Order and Chaos spells with combined growth."),

    PALADIN(HeroClass.ORDER, HeroClass.WARRIOR,
            "Paladin",
            "Berserker Attack now heals the Paladin for 10% of their max HP before launching the attack."),

    PROPHET(HeroClass.ORDER, HeroClass.MAGE,
            "Prophet",
            "Friendly spells (Protect, Heal, Replenish) now double their effect."),

    // Chaos
    INVOKER(HeroClass.CHAOS, HeroClass.CHAOS,
            "Invoker",
            "Chain Lightning now deals 50% damage (instead of 25%) to every subsequent target hit."),

    ROGUE(HeroClass.CHAOS, HeroClass.WARRIOR,
            "Rogue",
            "Gain Sneak Attack: every attack has a 50% chance to deal an additional hit for 50% of total damage."),

    SORCERER(HeroClass.CHAOS, HeroClass.MAGE,
            "Sorcerer",
            "Fireball now causes double damage to ALL units hit."),

    // Warrior
    KNIGHT(HeroClass.WARRIOR, HeroClass.WARRIOR,
            "Knight",
            "Berserker Attack now has a 50% chance to stun every unit it hits (they miss their next turn)."),

    WARLOCK(HeroClass.WARRIOR, HeroClass.MAGE,
            "Warlock",
            "Mana Burn: every time you attack a unit you burn 10% of its total mana points."),

    // Mage 
    WIZARD(HeroClass.MAGE, HeroClass.MAGE,
            "Wizard",
            "Replenish now costs only 40 mana instead of 80.");


    private final HeroClass classA;
    private final HeroClass classB;
    private final String    displayName;
    private final String    abilityDescription;

    HybridClass(HeroClass classA, HeroClass classB, String displayName, String abilityDescription) {
        this.classA            = classA;
        this.classB            = classB;
        this.displayName       = displayName;
        this.abilityDescription = abilityDescription;
    }

    public String getDisplayName()        { return displayName; }
    public String getAbilityDescription() { return abilityDescription; }
    public HeroClass getClassA()          { return classA; }
    public HeroClass getClassB()          { return classB; }

    /**
     * Combined attack bonus per level (sum of both contributing classes).
     */
    public int bonusAttackPerLevel()  { return classA.bonusAttackPerLevel()  + classB.bonusAttackPerLevel(); }
    public int bonusDefensePerLevel() { return classA.bonusDefensePerLevel() + classB.bonusDefensePerLevel(); }
    public int bonusHpPerLevel()      { return classA.bonusHpPerLevel()      + classB.bonusHpPerLevel(); }
    public int bonusManaPerLevel()    { return classA.bonusManaPerLevel()    + classB.bonusManaPerLevel(); }

    /**
     * Order-independent lookup. Returns null if no hybrid exists for the pair.
     */
    public static HybridClass of(HeroClass a, HeroClass b) {
        for (HybridClass h : values()) {
            if ((h.classA == a && h.classB == b) || (h.classA == b && h.classB == a)) {
                return h;
            }
        }
        return null;
    }

    @Override
    public String toString() { return displayName; }
}