package com.legendsofswordandwand.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Chaos class: +3 attack, +5 health per level.
 * Spells: Fireball (30 mana), Chain Lightning (40 mana).
 */
public class ChaosClass implements HeroClass {

    @Override
    public String getClassName() { return "Chaos"; }

    @Override
    public void applyLevelUpBonus(Hero hero) {
        hero.setAttack(hero.getAttack() + 3);
        hero.setMaxHp(hero.getMaxHp() + 5);
        hero.setCurrentHp(Math.min(hero.getCurrentHp() + 5, hero.getMaxHp()));
    }

    @Override
    public List<SpecialAbility> getSpecialAbilities() {
        return Arrays.asList(new FireballAbility(), new ChainLightningAbility());
    }

    // ---- Inner ability classes ----

    public static class FireballAbility implements SpecialAbility {
        @Override public String getName() { return "Fireball"; }
        @Override public int getManaCost() { return 30; }
        @Override public String getDescription() {
            return "Launch a fire attack that affects at most 3 enemy units.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Fireball!";
            List<Hero> aliveEnemies = new ArrayList<>();
            for (Hero e : enemies) { if (e.isAlive()) aliveEnemies.add(e); }
            int count = Math.min(3, aliveEnemies.size());
            StringBuilder log = new StringBuilder(caster.getName() + " launches Fireball!\n");
            for (int i = 0; i < count; i++) {
                Hero t = aliveEnemies.get(i);
                int dmg = t.takeDamage(caster.getAttack());
                log.append("  ").append(t.getName()).append(" takes ").append(dmg).append(" fire damage.\n");
            }
            return log.toString().trim();
        }
    }

    public static class ChainLightningAbility implements SpecialAbility {
        @Override public String getName() { return "Chain Lightning"; }
        @Override public int getManaCost() { return 40; }
        @Override public String getDescription() {
            return "Hits first target for 100% ATK, each subsequent for 25% of previous.";
        }
        @Override
        public String execute(Hero caster, List<Hero> targets, List<Hero> allies, List<Hero> enemies) {
            if (!caster.spendMana(getManaCost()))
                return caster.getName() + " doesn't have enough mana for Chain Lightning!";
            List<Hero> aliveEnemies = new ArrayList<>();
            for (Hero e : enemies) { if (e.isAlive()) aliveEnemies.add(e); }
            if (aliveEnemies.isEmpty()) return "No enemies to chain lightning!";
            // Shuffle to randomize order, first target is targets.get(0) if provided
            if (!targets.isEmpty() && aliveEnemies.contains(targets.get(0))) {
                aliveEnemies.remove(targets.get(0));
                aliveEnemies.add(0, targets.get(0));
            } else {
                Collections.shuffle(aliveEnemies);
            }
            StringBuilder log = new StringBuilder(caster.getName() + " casts Chain Lightning!\n");
            double damageMult = 1.0;
            for (Hero t : aliveEnemies) {
                int rawDmg = (int)(caster.getAttack() * damageMult);
                int dmg = t.takeDamage(rawDmg);
                log.append("  ").append(t.getName()).append(" takes ").append(dmg).append(" lightning damage.\n");
                damageMult *= 0.25;
            }
            return log.toString().trim();
        }
    }
}
