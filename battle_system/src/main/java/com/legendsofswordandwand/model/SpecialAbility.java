package com.legendsofswordandwand.model;

import java.util.List;

/**
 * Represents a special ability or spell a hero can cast during battle.
 * Command pattern: each ability encapsulates its execution logic.
 */
public interface SpecialAbility {

    /** The display name of this ability. */
    String getName();

    /** The mana cost to cast this ability. */
    int getManaCost();

    /** A short description of what this ability does. */
    String getDescription();

    /**
     * Execute the ability.
     * @param caster  The hero casting the ability.
     * @param targets List of target heroes (allies or enemies depending on ability).
     * @param allies  The caster's party.
     * @param enemies The opposing party.
     * @return A log message describing what happened.
     */
    String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies);
}
