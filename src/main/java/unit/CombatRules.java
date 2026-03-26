package unit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CombatRules {

  private CombatRules() {}

  public static int basicDamage(Unit attacker, Unit defender) {
    return Math.max(0, attacker.attack() - defender.defense());
  }

  public static int performAttack(Unit attacker, Unit target, List<Unit> enemyTeam) {
    int damage = basicDamage(attacker, target);
    applyDamage(attacker, target, damage);

    if (attacker instanceof Hero hero) {
      applyOnAttackEffects(hero, enemyTeam, target, damage);
    }
    return damage;
  }

  public static void applyDamage(Unit attacker, Unit target, int damage) {
    boolean fireShieldActive = target instanceof Hero hero && hero.hasFireShield();
    target.takeDamage(Math.max(0, damage));
    if (fireShieldActive && attacker.isAlive()) {
      attacker.takeDamage(Math.max(0, damage / 10));
    }
  }

  private static void applyOnAttackEffects(Hero attacker, List<Unit> enemyTeam, Unit primaryTarget, int damage) {
    if (attacker.isHybrid(HybridClass.WARLOCK)) {
      primaryTarget.burnManaByPercent(10);
    }
    if (attacker.isHybrid(HybridClass.ROGUE) && enemyTeam.stream().anyMatch(Unit::isAlive)
        && ThreadLocalRandom.current().nextBoolean()) {
      List<Unit> candidates = enemyTeam.stream()
          .filter(Unit::isAlive)
          .toList();
      Unit bonusTarget = candidates.isEmpty()
          ? null
          : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
      if (bonusTarget != null) {
        applyDamage(attacker, bonusTarget, Math.max(0, damage / 2));
      }
    }
  }
}
