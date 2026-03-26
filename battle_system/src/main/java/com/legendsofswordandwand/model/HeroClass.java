package com.legendsofswordandwand.model;

import java.util.List;

/**
 * Strategy pattern interface for hero class behaviors.
 * Each class defines its own level-up bonuses and special abilities.
 */
public interface HeroClass {

    /** Returns the display name of this class. */
    String getClassName();

    /**
     * Apply stat bonuses when a hero of this class gains a level.
     * Called AFTER the base level-up stats have been applied.
     */
    void applyLevelUpBonus(Hero hero);

    /**
     * Returns the list of special abilities available to this class.
     */
    List<SpecialAbility> getSpecialAbilities();
}
