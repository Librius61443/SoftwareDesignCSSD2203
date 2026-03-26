package com.legends.model.hero;

import java.util.EnumMap;
import java.util.Map;

/* 
System to track levels invested in each class
Handles class hybridization and specialization
Handles all class-related logic
*/
public class ClassLevelTracker {
    private static final int SPECIALIZATION_THRESHOLD = 5;
    //Track levels invested in each class
    private final Map<HeroClass, Integer> classLevels = new EnumMap<>(HeroClass.class);
    //Set once hybridization occurs
    private HybridClass hybridClass = null;
    //Set once first class reaches lvl 5, cleared when hybridized
    private HeroClass specializationClass = null;

    //Constructor
    public ClassLevelTracker(){
        for(HeroClass hc : HeroClass.values()){
            classLevels.put(hc, 0); //Set all class levels to 0
        }
    }
    //Constructor for loading from save data
    public ClassLevelTracker(Map<HeroClass, Integer> saved, HybridClass hybrid, HeroClass spec){
        for(HeroClass hc: HeroClass.values()){
            classLevels.put(hc, saved.getOrDefault(hc, 0));
        }
        this.hybridClass = hybrid;
        this.specializationClass = spec;
    }
    //Return info upon lvl up
    public static class LevelUpResult{
        public final HeroClass classChosen;
        public final HeroClass newSpecialization;
        public final HybridClass newHybrid;
        public final boolean justSpecialized;
        public final boolean justHybridized;

        public LevelUpResult(HeroClass classChosen, HeroClass spec, HybridClass hybrid, boolean justSpecialized, boolean justHybridized) {
            this.classChosen = classChosen;
            this.newSpecialization = spec;
            this.newHybrid = hybrid;
            this.justSpecialized = justSpecialized;
            this.justHybridized = justHybridized;
        }
        // Notify if specialized/hybridized on lvl up
        @Override
        public String toString(){
            if(justHybridized) return "Hybridized: " +newHybrid;
            if(justSpecialized) return "Specialized: "+newSpecialization;
            return "Levelled up class: "+classChosen;
        }
    }
    //Invest a level into chosen class
    public LevelUpResult investLevel(HeroClass chosen){
        if(hybridClass != null){
            classLevels.merge(chosen, 1, Integer::sum);
            return new LevelUpResult(chosen, specializationClass, hybridClass, false, false);
        }
        classLevels.merge(chosen, 1, Integer::sum);
        int newLevel = classLevels.get(chosen);
        boolean justSpecialized = false;
        boolean justHybridized = false;

        if(newLevel >= SPECIALIZATION_THRESHOLD){
            if(specializationClass == null){ //First specialization
                specializationClass = chosen;
                justSpecialized = true;
            }
            else if (specializationClass != chosen){ // Hybridization
                hybridClass = HybridClass.of(specializationClass, chosen);
                specializationClass = null;
                justHybridized = true;
            }
        }

        return new LevelUpResult(chosen, specializationClass, hybridClass, justSpecialized, justHybridized);
    }


    /*Stat increase helpers 
    Growth mult is only 2 if no hybrid, and specialized
    Called by Hero class
    */
    private int growthMultiplier(HeroClass chosen) {
        return (hybridClass == null && specializationClass != null
                && specializationClass == chosen) ? 2 : 1;
    }
    public int attackBonusForLevel(HeroClass chosen) {
        int base = (hybridClass != null)
                ? hybridClass.bonusAttackPerLevel()
                : chosen.bonusAttackPerLevel();
        return base * growthMultiplier(chosen);
    }
 
    public int defenseBonusForLevel(HeroClass chosen) {
        int base = (hybridClass != null)
                ? hybridClass.bonusDefensePerLevel()
                : chosen.bonusDefensePerLevel();
        return base * growthMultiplier(chosen);
    }
 
    public int hpBonusForLevel(HeroClass chosen) {
        int base = (hybridClass != null)
                ? hybridClass.bonusHpPerLevel()
                : chosen.bonusHpPerLevel();
        return base * growthMultiplier(chosen);
    }
 
    public int manaBonusForLevel(HeroClass chosen) {
        int base = (hybridClass != null)
                ? hybridClass.bonusManaPerLevel()
                : chosen.bonusManaPerLevel();
        return base * growthMultiplier(chosen);
    }
    //Getters
    public int getLevelInClass(HeroClass hc)     { return classLevels.getOrDefault(hc, 0); }
    public Map<HeroClass, Integer> getAllLevels() { return new EnumMap<>(classLevels); }
    public HybridClass getHybridClass()          { return hybridClass; }
    public HeroClass getSpecializationClass()    { return specializationClass; }
    public boolean isHybridized()                { return hybridClass != null; }
    public boolean isSpecialized()               { return specializationClass != null; }


}
    
