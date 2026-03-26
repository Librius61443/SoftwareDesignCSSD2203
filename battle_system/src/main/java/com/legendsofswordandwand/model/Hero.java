package com.legendsofswordandwand.model;

/**
 * Represents a hero in the game.
 * Base stats at level 1: 5 attack, 5 defense, 100 HP, 50 mana.
 * Every level: +1 attack, +1 defense, +5 HP, +2 mana (before class bonuses).
 */
public class Hero {

    private String name;
    private HeroClass heroClass;
    private int level;

    // Base stats
    private int maxHp;
    private int currentHp;
    private int maxMana;
    private int currentMana;
    private int attack;
    private int defense;

    // Status flags
    private boolean stunned;
    private boolean defending;
    private boolean waiting;
    private int shieldHp; // temporary shield HP from Protect/Fire Shield

    // Experience
    private int experience;
    private int experienceToNextLevel;

    public Hero(String name, HeroClass heroClass) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = 1;
        // Base stats at level 1
        this.attack = 5;
        this.defense = 5;
        this.maxHp = 100;
        this.currentHp = 100;
        this.maxMana = 50;
        this.currentMana = 50;
        this.stunned = false;
        this.defending = false;
        this.waiting = false;
        this.shieldHp = 0;
        this.experience = 0;
        this.experienceToNextLevel = calculateExpToNextLevel(1);
        // Apply class bonuses for level 1 (no bonus at level 1, starts base)
    }

    /** Calculate experience needed to reach next level from current level L. */
    public static int calculateExpToNextLevel(int currentLevel) {
        // Exp(L) = Exp(L-1) + 500 + 75*L + 20*L^2
        // We track cumulative; here return just the increment for leveling up from currentLevel
        return 500 + 75 * (currentLevel + 1) + 20 * (currentLevel + 1) * (currentLevel + 1);
    }

    /**
     * Level up the hero, applying base stat increases and class bonuses.
     */
    public void levelUp() {
        if (level >= 20) return;
        level++;
        // Base level-up bonuses
        attack += 1;
        defense += 1;
        maxHp += 5;
        currentHp = Math.min(currentHp + 5, maxHp);
        maxMana += 2;
        currentMana = Math.min(currentMana + 2, maxMana);
        // Class-specific bonuses
        heroClass.applyLevelUpBonus(this);
        experienceToNextLevel = calculateExpToNextLevel(level);
    }

    /**
     * Apply damage to this hero. Shields absorb damage first.
     * @return actual HP damage taken
     */
    public int takeDamage(int rawDamage) {
        int damage = Math.max(0, rawDamage - defense);
        if (shieldHp > 0) {
            int absorbed = Math.min(shieldHp, damage);
            shieldHp -= absorbed;
            damage -= absorbed;
        }
        currentHp = Math.max(0, currentHp - damage);
        return damage;
    }

    /** Heal this hero by amount, capped at maxHp. */
    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    /** Add a shield of given HP. Shields stack additively. */
    public void addShield(int amount) {
        shieldHp += amount;
    }

    /** Use mana. Returns false if insufficient mana. */
    public boolean spendMana(int amount) {
        if (currentMana < amount) return false;
        currentMana -= amount;
        return true;
    }

    /** Restore mana, capped at maxMana. */
    public void restoreMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    /** Called when this hero chooses Defend action. */
    public void defend() {
        defending = true;
        currentHp = Math.min(maxHp, currentHp + 10);
        currentMana = Math.min(maxMana, currentMana + 5);
    }

    public boolean isAlive() { return currentHp > 0; }
    public boolean isStunned() { return stunned; }
    public void setStunned(boolean stunned) { this.stunned = stunned; }
    public boolean isDefending() { return defending; }
    public void setDefending(boolean defending) { this.defending = defending; }
    public boolean isWaiting() { return waiting; }
    public void setWaiting(boolean waiting) { this.waiting = waiting; }

    // Getters & Setters
    public String getName() { return name; }
    public HeroClass getHeroClass() { return heroClass; }
    public void setHeroClass(HeroClass heroClass) { this.heroClass = heroClass; }
    public int getLevel() { return level; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }
    public int getCurrentMana() { return currentMana; }
    public void setCurrentMana(int currentMana) { this.currentMana = currentMana; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getShieldHp() { return shieldHp; }
    public void setShieldHp(int shieldHp) { this.shieldHp = shieldHp; }
    public int getExperience() { return experience; }
    public void addExperience(int exp) { this.experience += exp; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public void setLevel(int level) { this.level = level; }

    @Override
    public String toString() {
        return String.format("%s [%s Lv%d] HP:%d/%d MP:%d/%d ATK:%d DEF:%d",
                name, heroClass.getClassName(), level, currentHp, maxHp, currentMana, maxMana, attack, defense);
    }
}
