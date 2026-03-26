package com.legendsofswordandwand.model;

import java.util.Collections;
import java.util.List;

/**
 * Mage class: +5 mana, +1 attack per level.
 * Spells: Replenish (80 mana).
 */
public class MageClass implements HeroClass {

    @Override
    public String getClassName() { return "Mage"; }

    @Override
    public void applyLevelUpBonus(Hero hero) {
        hero.setMaxMana(hero.getMaxMana() + 5);
        hero.setCurrentMana(Math.min(hero.getCurrentMana() + 5, hero.getMaxMana()));
        hero.setAttack(hero.getAttack() + 1);
    }

    @Override
    public List<SpecialAbility> getSpecialAbilities() {
        return Collections.singletonList(new ReplenishAbility());
    }

    public static class ReplenishAbility implements SpecialAbility {
        @Override public String getName() { return "Replenish"; }
        @Override public int getManaCost() { return 80; }
        @Override public String getDescription() {
            return "Restore 30 mana to all allies and 60 mana to self.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Replenish!";
            StringBuilder log = new StringBuilder(caster.getName() + " casts Replenish!\n");
            for (Hero ally : allies) {
                if (ally.isAlive() && ally != caster) {
                    ally.restoreMana(30);
                    log.append("  ").append(ally.getName()).append(" restores 30 mana.\n");
                }
            }
            caster.restoreMana(60);
            log.append("  ").append(caster.getName()).append(" restores 60 mana to self.");
            return log.toString();
        }
    }
}
