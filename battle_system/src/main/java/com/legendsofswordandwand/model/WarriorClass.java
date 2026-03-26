package com.legendsofswordandwand.model;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Warrior class: +2 attack, +3 defense per level.
 * Spells: Berserker Attack (60 mana).
 */
public class WarriorClass implements HeroClass {

    @Override
    public String getClassName() { return "Warrior"; }

    @Override
    public void applyLevelUpBonus(Hero hero) {
        hero.setAttack(hero.getAttack() + 2);
        hero.setDefense(hero.getDefense() + 3);
    }

    @Override
    public List<SpecialAbility> getSpecialAbilities() {
        return Collections.singletonList(new BerserkerAttackAbility());
    }

    public static class BerserkerAttackAbility implements SpecialAbility {
        @Override public String getName() { return "Berserker Attack"; }
        @Override public int getManaCost() { return 60; }
        @Override public String getDescription() {
            return "Attack target, then damage 2 more units for 25% of original damage.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Berserker Attack!";
            if (targets.isEmpty()) return "No target selected!";
            StringBuilder log = new StringBuilder(caster.getName() + " goes berserk!\n");
            Hero primary = targets.get(0);
            int primaryDmg = primary.takeDamage(caster.getAttack());
            log.append("  ").append(primary.getName()).append(" takes ").append(primaryDmg).append(" damage.\n");
            // Hit 2 additional enemies for 25%
            List<Hero> others = new ArrayList<>();
            for (Hero e : enemies) { if (e.isAlive() && e != primary) others.add(e); }
            int splashCount = Math.min(2, others.size());
            int splashDmg = (int)(primaryDmg * 0.25);
            for (int i = 0; i < splashCount; i++) {
                int dmg = others.get(i).takeDamage(splashDmg);
                log.append("  ").append(others.get(i).getName()).append(" takes ").append(dmg).append(" splash damage.\n");
            }
            return log.toString().trim();
        }
    }
}
