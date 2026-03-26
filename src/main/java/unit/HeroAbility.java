package unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum HeroAbility {
  PROTECT(25) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      if (!caster.spendMana(manaCost())) return;
      int shieldPercent = caster.isHybrid(HybridClass.PROPHET) ? 20 : 10;
      boolean fireShield = caster.isHybrid(HybridClass.HERETIC);
      allies.stream()
          .filter(Hero.class::isInstance)
          .map(Hero.class::cast)
          .filter(Hero::isAlive)
          .forEach(hero -> {
            hero.gainShield(Math.max(1, (hero.maxHp() * shieldPercent) / 100));
            hero.setFireShield(fireShield);
          });
    }
  },
  HEAL(35) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      if (!caster.spendMana(manaCost())) return;
      List<Hero> targets;
      if (caster.isSpecializedIn(HeroClass.ORDER)) {
        targets = allies.stream()
            .filter(Hero.class::isInstance)
            .map(Hero.class::cast)
            .filter(Hero::isAlive)
            .toList();
      } else {
        Hero lowestHpHero = allies.stream()
          .filter(Hero.class::isInstance)
          .map(Hero.class::cast)
          .filter(Hero::isAlive)
          .min(Comparator.comparingInt(Hero::hp))
          .orElse(null);
        targets = lowestHpHero == null ? List.of() : List.of(lowestHpHero);
      }
      int healPercent = caster.isHybrid(HybridClass.PROPHET) ? 50 : 25;
      for (Hero target : targets) {
        target.heal(Math.max(1, (target.maxHp() * healPercent) / 100));
      }
    }
  },
  FIREBALL(30) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      if (!caster.spendMana(manaCost())) return;
      List<Unit> targets = selectedTargets.stream()
          .filter(Unit::isAlive)
          .limit(3)
          .toList();
      if (targets.isEmpty()) {
        targets = enemies.stream().filter(Unit::isAlive).limit(3).toList();
      }
      int multiplier = caster.isHybrid(HybridClass.SORCERER) ? 2 : 1;
      targets.forEach(target -> CombatRules.applyDamage(caster, target, baseDamage(caster, target) * multiplier));
    }
  },
  CHAIN_LIGHTNING(40) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      if (!caster.spendMana(manaCost())) return;
      List<Unit> aliveEnemies = new ArrayList<>(enemies.stream().filter(Unit::isAlive).toList());
      if (!selectedTargets.isEmpty() && aliveEnemies.remove(selectedTargets.get(0))) {
        Collections.shuffle(aliveEnemies);
        aliveEnemies.add(0, selectedTargets.get(0));
      }
      int damage = 0;
      for (int i = 0; i < aliveEnemies.size(); i++) {
        Unit target = aliveEnemies.get(i);
        int divisor = caster.isSpecializedIn(HeroClass.CHAOS) ? 2 : 4;
        damage = i == 0 ? baseDamage(caster, target) : Math.max(0, damage / divisor);
        CombatRules.applyDamage(caster, target, damage);
      }
    }
  },
  BERSERKER_ATTACK(60) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      if (!caster.spendMana(manaCost())) return;
      List<Unit> aliveEnemies = enemies.stream().filter(Unit::isAlive).toList();
      if (aliveEnemies.isEmpty()) return;

      Unit primary = !selectedTargets.isEmpty() && selectedTargets.get(0).isAlive()
          ? selectedTargets.get(0)
          : aliveEnemies.get(0);
      if (caster.isHybrid(HybridClass.PALADIN)) {
        caster.heal(Math.max(1, caster.maxHp() / 10));
      }
      int primaryDamage = baseDamage(caster, primary);
      CombatRules.applyDamage(caster, primary, primaryDamage);
      maybeStunTarget(caster, primary);

      int splashDamage = Math.max(0, primaryDamage / 4);
      int splashHits = 0;
      for (Unit target : aliveEnemies) {
        if (target == primary || splashHits >= 2) continue;
        CombatRules.applyDamage(caster, target, splashDamage);
        maybeStunTarget(caster, target);
        splashHits++;
      }
    }
  },
  REPLENISH(80) {
    @Override
    public void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets) {
      int manaCost = caster.isSpecializedIn(HeroClass.MAGE) ? 40 : manaCost();
      if (!caster.spendMana(manaCost)) return;
      int allyRestore = caster.isHybrid(HybridClass.PROPHET) ? 60 : 30;
      int selfRestore = caster.isHybrid(HybridClass.PROPHET) ? 120 : 60;
      allies.stream()
          .filter(Hero.class::isInstance)
          .map(Hero.class::cast)
          .filter(hero -> hero != caster && hero.isAlive())
          .forEach(hero -> hero.restoreMana(allyRestore));
      caster.restoreMana(selfRestore);
    }
  };

  private final int manaCost;

  HeroAbility(int manaCost) {
    this.manaCost = manaCost;
  }

  public int manaCost() {
    return manaCost;
  }

  public abstract void execute(Hero caster, List<Unit> allies, List<Unit> enemies, List<Unit> selectedTargets);

  public void execute(Hero caster, List<Unit> allies, List<Unit> enemies) {
    execute(caster, allies, enemies, List.of());
  }

  private static int baseDamage(Unit attacker, Unit defender) {
    return CombatRules.basicDamage(attacker, defender);
  }

  private static void maybeStunTarget(Hero caster, Unit target) {
    if (caster.isSpecializedIn(HeroClass.WARRIOR) && target.isAlive()
        && ThreadLocalRandom.current().nextBoolean()) {
      target.setStunned(true);
    }
  }
}
