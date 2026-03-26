package unit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BattleAbilityTest {

  private Hero specialized(HeroClass heroClass) {
    Hero hero = new Hero("Spec-" + heroClass.name(), heroClass);
    while (hero.specializationClass() != heroClass) {
      hero.gainExp(5_000);
    }
    return hero;
  }

  private Hero hybrid(HeroClass firstClass, HeroClass secondClass) {
    Hero hero = new Hero("Hybrid-" + firstClass.name() + "-" + secondClass.name(), firstClass);
    while (hero.specializationClass() != firstClass) {
      hero.gainExp(5_000);
    }
    hero.setCurrentClass(secondClass);
    HybridClass expectedHybrid = HybridClass.of(firstClass, secondClass);
    while (hero.hybridClass() != expectedHybrid) {
      hero.gainExp(5_000);
    }
    return hero;
  }

  @Test
  void orderProtectAddsShieldToAllAllies() {
    Hero caster = new Hero("Order", HeroClass.ORDER);
    Hero ally = new Hero("Ally", HeroClass.WARRIOR);

    HeroAbility.PROTECT.execute(caster, List.of(caster, ally), List.of());

    assertTrue(caster.shield() > 0);
    assertTrue(ally.shield() > 0);
  }

  @Test
  void chaosFireballDamagesUpToThreeEnemies() {
    Hero caster = new Hero("Chaos", HeroClass.CHAOS);
    caster.restoreMana(100);
    EnemyUnit e1 = new EnemyUnit("e1", 1, 7, 0, 100, 50);
    EnemyUnit e2 = new EnemyUnit("e2", 1, 7, 0, 100, 50);
    EnemyUnit e3 = new EnemyUnit("e3", 1, 7, 0, 100, 50);
    EnemyUnit e4 = new EnemyUnit("e4", 1, 7, 0, 100, 50);

    HeroAbility.FIREBALL.execute(caster, List.of(caster), List.of(e1, e2, e3, e4));

    assertTrue(e1.hp() < e1.maxHp());
    assertTrue(e2.hp() < e2.maxHp());
    assertTrue(e3.hp() < e3.maxHp());
    assertEquals(e4.maxHp(), e4.hp());
  }

  @Test
  void mageReplenishRestoresManaToParty() {
    Hero caster = new Hero("Mage", HeroClass.MAGE);
    Hero ally = new Hero("Ally", HeroClass.ORDER);
    caster.gainExp(10_000);
    caster.restoreMana(1_000);
    caster.spendMana(10);
    ally.spendMana(20);

    HeroAbility.REPLENISH.execute(caster, List.of(caster, ally), List.of());

    assertTrue(caster.mana() >= 60);
    assertTrue(ally.mana() > 30);
  }

  @Test
  void priestHealAppliesToAllFriendlyUnits() {
    Hero caster = specialized(HeroClass.ORDER);
    Hero ally = new Hero("Ally", HeroClass.WARRIOR);
    caster.takeDamage(40);
    ally.takeDamage(40);

    HeroAbility.HEAL.execute(caster, List.of(caster, ally), List.of());

    assertTrue(caster.hp() > 60);
    assertTrue(ally.hp() > 60);
  }

  @Test
  void hereticProtectCreatesFireShieldRetaliation() {
    Hero caster = hybrid(HeroClass.ORDER, HeroClass.CHAOS);
    Hero ally = new Hero("Protected", HeroClass.WARRIOR);
    EnemyUnit attacker = new EnemyUnit("Enemy", 1, 20, 0, 100, 50);

    HeroAbility.PROTECT.execute(caster, List.of(caster, ally), List.of());
    int attackerHpBefore = attacker.hp();
    assertTrue(ally.hasFireShield());

    CombatRules.performAttack(attacker, ally, List.of(caster, ally));

    assertTrue(attacker.hp() < attackerHpBefore);
  }

  @Test
  void invokerChainLightningUsesHigherCarryoverDamage() {
    Hero caster = specialized(HeroClass.CHAOS);
    EnemyUnit e1 = new EnemyUnit("e1", 1, 0, 0, 100, 50);
    EnemyUnit e2 = new EnemyUnit("e2", 1, 0, 0, 100, 50);
    EnemyUnit e3 = new EnemyUnit("e3", 1, 0, 0, 100, 50);

    HeroAbility.CHAIN_LIGHTNING.execute(caster, List.of(caster), List.of(e1, e2, e3), List.of(e1));

    int firstHit = CombatRules.basicDamage(caster, e1);
    int secondHit = Math.max(0, firstHit / 2);
    int thirdHit = Math.max(0, secondHit / 2);
    assertEquals(e1.maxHp() - firstHit, e1.hp());
    List<Integer> remainingHp = List.of(e2.hp(), e3.hp()).stream().sorted().toList();
    List<Integer> expectedHp = List.of(e2.maxHp() - secondHit, e3.maxHp() - thirdHit).stream().sorted().toList();
    assertEquals(expectedHp, remainingHp);
  }

  @Test
  void paladinBerserkerAttackHealsCasterBeforeDamage() {
    Hero caster = hybrid(HeroClass.ORDER, HeroClass.WARRIOR);
    EnemyUnit e1 = new EnemyUnit("e1", 1, 0, 0, 100, 50);
    EnemyUnit e2 = new EnemyUnit("e2", 1, 0, 0, 100, 50);
    EnemyUnit e3 = new EnemyUnit("e3", 1, 0, 0, 100, 50);
    caster.takeDamage(30);
    int hpBefore = caster.hp();

    HeroAbility.BERSERKER_ATTACK.execute(caster, List.of(caster), List.of(e1, e2, e3), List.of(e1));

    assertTrue(caster.hp() > hpBefore);
    assertTrue(e1.hp() < e1.maxHp());
    assertTrue(e2.hp() < e2.maxHp());
    assertTrue(e3.hp() < e3.maxHp());
  }

  @Test
  void prophetFriendlySpellsDoubleTheirEffect() {
    Hero caster = hybrid(HeroClass.ORDER, HeroClass.MAGE);
    Hero ally = new Hero("Ally", HeroClass.WARRIOR);
    caster.takeDamage(60);
    ally.takeDamage(60);
    caster.spendMana(10);
    ally.spendMana(30);

    HeroAbility.HEAL.execute(caster, List.of(caster, ally), List.of());
    assertEquals(Math.min(ally.maxHp(), ally.maxHp() - 60 + Math.max(1, ally.maxHp() / 2)), ally.hp());

    caster.restoreMana(200);
    HeroAbility.REPLENISH.execute(caster, List.of(caster, ally), List.of());
    assertEquals(ally.maxMana(), ally.mana());

    caster.restoreMana(200);
    HeroAbility.PROTECT.execute(caster, List.of(caster, ally), List.of());
    assertTrue(caster.shield() >= caster.maxHp() / 5);
    assertTrue(ally.shield() >= ally.maxHp() / 5);
  }

  @Test
  void attacksDealZeroWhenDefenseExceedsAttack() {
    Hero attacker = new Hero("Attacker", HeroClass.ORDER);
    EnemyUnit defender = new EnemyUnit("Tank", 1, 0, 10, 100, 50);

    int damage = CombatRules.performAttack(attacker, defender, List.of(defender));

    assertEquals(0, damage);
    assertEquals(defender.maxHp(), defender.hp());
  }

  @Test
  void starterHeroCanDamageGeneratedLevelOneEnemy() {
    Hero starter = new Hero("Starter", HeroClass.WARRIOR);
    EnemyUnit enemy = new EnemyUnit("Enemy", 1, 6, 3, 100, 50);

    int damage = CombatRules.performAttack(starter, enemy, List.of(enemy));

    assertTrue(damage > 0);
    assertTrue(enemy.hp() < enemy.maxHp());
  }

  @Test
  void enemiesDoNotDefendForeverWhenCritical() {
    EnemyUnit enemy = new EnemyUnit("Enemy", 1, 6, 3, 100, 50);

    enemy.takeDamage(85);
    Action firstAction = enemy.chooseAction(List.of(), List.of(), new java.util.ArrayDeque<>());
    firstAction.execute(enemy, List.of(), List.of());
    assertEquals(25, enemy.hp());

    enemy.takeDamage(10);
    Action secondAction = enemy.chooseAction(List.of(), List.of(), new java.util.ArrayDeque<>());
    assertEquals(ActionType.ATTACK, secondAction.type());
  }

  @Test
  void sorcererFireballDealsDoubleDamage() {
    Hero caster = hybrid(HeroClass.CHAOS, HeroClass.MAGE);
    EnemyUnit enemy = new EnemyUnit("Enemy", 1, 0, 0, 100, 50);

    HeroAbility.FIREBALL.execute(caster, List.of(caster), List.of(enemy), List.of(enemy));

    assertEquals(enemy.maxHp() - (CombatRules.basicDamage(caster, enemy) * 2), enemy.hp());
  }

  @Test
  void knightBerserkerAttackCanStunEnemyUnits() {
    Hero caster = specialized(HeroClass.WARRIOR);
    for (int attempt = 0; attempt < 40; attempt++) {
      EnemyUnit target = new EnemyUnit("Enemy-" + attempt, 1, 0, 0, 100, 50);
      HeroAbility.BERSERKER_ATTACK.execute(caster.copy(), List.of(caster), List.of(target), List.of(target));
      if (target.stunned()) {
        assertTrue(target.stunned());
        return;
      }
    }
    fail("Expected Knight specialization to stun at least one enemy across repeated trials.");
  }

  @Test
  void warlockBasicAttacksBurnMana() {
    Hero caster = hybrid(HeroClass.WARRIOR, HeroClass.MAGE);
    EnemyUnit target = new EnemyUnit("Enemy", 1, 0, 0, 100, 80);
    int manaBefore = target.mana();

    CombatRules.performAttack(caster, target, List.of(target));

    assertTrue(target.mana() < manaBefore);
  }

  @Test
  void rogueBasicAttacksCanTriggerSneakAttack() {
    Hero caster = hybrid(HeroClass.CHAOS, HeroClass.WARRIOR);
    for (int attempt = 0; attempt < 40; attempt++) {
      EnemyUnit targetA = new EnemyUnit("A", 1, 0, 0, 100, 50);
      EnemyUnit targetB = new EnemyUnit("B", 1, 0, 0, 100, 50);

      CombatRules.performAttack(caster, targetA, List.of(targetA, targetB));

      if (targetB.hp() < targetB.maxHp() || targetA.hp() <= 93) {
        assertTrue(true);
        return;
      }
    }
    fail("Expected Rogue hybrid to trigger a sneak attack across repeated trials.");
  }

  @Test
  void wizardSpecializationMakesReplenishCheaper() {
    Hero caster = specialized(HeroClass.MAGE);
    Hero ally = new Hero("Ally", HeroClass.ORDER);
    caster.spendMana(10);
    ally.spendMana(20);
    int manaBefore = caster.mana();

    HeroAbility.REPLENISH.execute(caster, List.of(caster, ally), List.of());

    assertEquals(manaBefore + 20, caster.mana());
  }
}
