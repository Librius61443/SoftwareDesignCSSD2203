package com.legendsofswordandwand.model;

import java.util.Arrays;
import java.util.List;

/**
 * Order class: +5 mana, +2 defense per level.
 * Spells: Protect (25 mana), Heal (35 mana).
 */
public class OrderClass implements HeroClass {

    @Override
    public String getClassName() { return "Order"; }

    @Override
    public void applyLevelUpBonus(Hero hero) {
        hero.setMaxMana(hero.getMaxMana() + 5);
        hero.setCurrentMana(Math.min(hero.getCurrentMana() + 5, hero.getMaxMana()));
        hero.setDefense(hero.getDefense() + 2);
    }

    @Override
    public List<SpecialAbility> getSpecialAbilities() {
        return Arrays.asList(new ProtectAbility(), new HealAbility());
    }

    // ---- Inner ability classes ----

    public static class ProtectAbility implements SpecialAbility {
        @Override public String getName() { return "Protect"; }
        @Override public int getManaCost() { return 25; }
        @Override public String getDescription() {
            return "Cast a shield on all party members for 10% of each hero's max HP.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Protect!";
            StringBuilder log = new StringBuilder(caster.getName() + " casts Protect!\n");
            for (Hero ally : allies) {
                if (ally.isAlive()) {
                    int shield = (int)(ally.getMaxHp() * 0.10);
                    ally.addShield(shield);
                    log.append("  ").append(ally.getName()).append(" gains ").append(shield).append(" shield HP.\n");
                }
            }
            return log.toString().trim();
        }
    }

    public static class HealAbility implements SpecialAbility {
        @Override public String getName() { return "Heal"; }
        @Override public int getManaCost() { return 35; }
        @Override public String getDescription() {
            return "Heal the ally with the lowest HP for 25% of their max HP.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Heal!";
            Hero target = allies.stream().filter(Hero::isAlive)
                    .min((a, b) -> Integer.compare(a.getCurrentHp(), b.getCurrentHp()))
                    .orElse(null);
            if (target == null) return "No valid heal target.";
            int healAmount = (int)(target.getMaxHp() * 0.25);
            target.heal(healAmount);
            return caster.getName() + " heals " + target.getName() + " for " + healAmount + " HP!";
        }
    }
}
