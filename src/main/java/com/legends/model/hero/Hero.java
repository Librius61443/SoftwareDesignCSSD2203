package com.legends.model.hero;

import java.util.ArrayList;
import java.util.List;

import com.legends.model.item.Item;

public class Hero {
    public static final int MAX_LEVEL = 20; 

    //Base Per-lvl Stat gains
    private static final int ATK_PER_LVL = 1;
    private static final int DEF_PER_LVL = 1;
    private static final int HP_PER_LVL = 5;
    private static final int MANA_PER_LVL = 2;

    private String id; //MongoDB id string
    private String name;

    private int level;
    private int exp;

    private HeroClass currentClass;
    private final ClassLevelTracker classTracker;

    private int atk;
    private int def;
    private int maxHp;
    private int maxMana;

    // Combat stats; Reset on inn visit
    private int currHp;
    private int currMana;
    private int shield;
    private boolean stunned;

    private final List<Item> inventory = new ArrayList<>();
    
    //Constructor
    public Hero(String name, HeroClass startingClass){
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.currentClass = startingClass;
        this.classTracker = new ClassLevelTracker();
        classTracker.investLevel(startingClass);

        this.atk = 5;
        this.def = 5;
        this.maxHp = 100;
        this.maxMana = 50;
        
        this.currHp = maxHp;
        this.currMana = maxMana;
        this.shield = 0;
        this.stunned = false;
    }
    //Constructor for loading from DB
    public Hero(String id, String name, int level, int experience, HeroClass currentClass,
                ClassLevelTracker tracker, int attack, int defense, int maxHp, int maxMana,
                int currentHp, int currentMana, int shield) {
        this.id           = id;
        this.name         = name;
        this.level        = level;
        this.exp   = experience;
        this.currentClass = currentClass;
        this.classTracker = tracker;
        this.atk       = attack;
        this.def      = defense;
        this.maxHp        = maxHp;
        this.maxMana      = maxMana;
        this.currHp    = currentHp;
        this.currMana  = currentMana;
        this.shield       = shield;
        this.stunned      = false;
    }
    // Leveling System
    public boolean gainExp(int amt){
        exp += amt;
        boolean levelled = false;
        while(level < MAX_LEVEL && exp >= expToNextLevel()){
            exp -= expToNextLevel();
            levelled = true;
        }
        return levelled;
    }
    //Applies a level up using a given class choice
    public ClassLevelTracker.LevelUpResult levelUp(HeroClass chosenClass){
        if (level >= MAX_LEVEL) throw new IllegalStateException("Hero Already at max level, Try a diferent class.");
        this.currentClass = chosenClass;
        ClassLevelTracker.LevelUpResult result = classTracker.investLevel(chosenClass);
        atk += ATK_PER_LVL + classTracker.attackBonusForLevel(chosenClass);
        def += DEF_PER_LVL + classTracker.defenseBonusForLevel(chosenClass);
        maxHp += HP_PER_LVL + classTracker.hpBonusForLevel(chosenClass);
        maxMana += MANA_PER_LVL + classTracker.manaBonusForLevel(chosenClass);

        //Level up restore
        currHp = maxHp;
        currMana = maxMana;

        level++;
        return result;

    //xp helper functions
    }
    public int expToNextLevel(){
        return 500+75*level+20*level*level;
    }
    public boolean canLevelUp(){
        return level< MAX_LEVEL && exp >= expToNextLevel();
    }

    //General Combat functions
    public int calcDamage(Hero defender){
        return Math.max(0, this.atk - defender.def);
    }
    public int takeDamage(int dmg){
        int shieldAbsorbed = Math.min(shield, dmg);
        shield -= shieldAbsorbed;
        int remaining = dmg - shieldAbsorbed;
        currHp = Math.max(0,currHp - remaining);
        return remaining; // dmg taken after shields
    }

    public boolean hasFireShield(){ //Just for Heretic spell
        return classTracker.getHybridClass() == HybridClass.HERETIC && shield > 0;
    }
    public void defend(){
        currHp = Math.min(maxHp, currHp+10);
        currMana = Math.min(maxMana, currMana + 5);
    }
    public void gainMana(int amt){
        currMana = Math.min(maxMana, currMana+amt);
    }
    public void gainHp(int amt){
        currHp = Math.min(maxHp, currHp+amt);
    }
    public void gainShield(int amt){
        shield += amt;
    }
    public void setStunned(boolean stunned){
        this.stunned = stunned;
    }
    public boolean isAlive(){
        return currHp > 0;
    }
    public boolean isStunned(){
        return stunned;
    }
    public boolean consumeStun(){ //Start of each heros turn
        if (stunned){
            stunned = false;
            return true;
        }
        return false;
    }

    public void fullRestore(){ //This if for the inn
        currHp = maxHp;
        currMana = maxMana;
        shield = 0;
        stunned = false;
    }
    public void revive(){
        if(currHp <= 0) currHp = 1;
        stunned = false;
    }
    
    //Inventory
    public void addItem(Item item){
        inventory.add(item);
    }
    public boolean useItem(Item item){
        if(!inventory.contains(item)) return false;
        item.applyTo(this);
        inventory.remove(item);
        return true;
    }
    public List<Item> getInventory(){
        return new ArrayList<>(inventory);
    }

    public void defeatExpPenalty(){ //Combat loss penalty, xp
        int penalty = (int)(exp*0.3);
        exp = Math.max(0, exp - penalty);
    }


    // Getters Setters
    public String getId()           { return id; }
    public void setId(String id)    { this.id = id; }
 
    public String getName()         { return name; }
    public void setName(String n)   { this.name = n; }
 
    public int getLevel()           { return level; }
    public int getExperience()      { return exp; }
 
    public HeroClass getCurrentClass()              { return currentClass; }
    public void setCurrentClass(HeroClass hc)       { this.currentClass = hc; }
    public ClassLevelTracker getClassTracker()       { return classTracker; }
 
    public HybridClass getHybridClass()             { return classTracker.getHybridClass(); }
    public HeroClass getSpecializationClass()        { return classTracker.getSpecializationClass(); }
 
    /** Display name: hybrid name if hybridized, else specialization, else current class. */
    public String getEffectiveClassName() {
        if (classTracker.isHybridized())   return classTracker.getHybridClass().getDisplayName();
        if (classTracker.isSpecialized())  return classTracker.getSpecializationClass().getDisplayName();
        return currentClass.getDisplayName();
    }
 
    public int getAttack()          { return atk; }
    public int getDefense()         { return def; }
    public int getMaxHp()           { return maxHp; }
    public int getMaxMana()         { return maxMana; }
    public int getCurrentHp()       { return currHp; }
    public int getCurrentMana()     { return currMana; }
    public int getShield()          { return shield; }
 
    public void setCurrentHp(int v)   { this.currHp   = Math.max(0, Math.min(maxHp,   v)); }
    public void setCurrentMana(int v) { this.currMana = Math.max(0, Math.min(maxMana,  v)); }
 
    @Override
    public String toString() {
        return String.format("%s [%s Lv%d | HP:%d/%d | MP:%d/%d | ATK:%d DEF:%d]",
                name, getEffectiveClassName(), level,
                currHp, maxHp, currMana, maxMana,
                atk, def);
    }

}
