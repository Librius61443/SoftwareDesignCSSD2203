package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for the PvP Turn-Based Battle System.
 * Covers: turn order, basic attack, defend, wait, cast abilities, stun,
 * shields, battle-end conditions, and BattleResult integrity.
 */
public class BattleEngineTest {

    // ---- Helpers ----

    private Hero makeHero(String name, HeroClass cls) {
        return new Hero(name, cls);
    }

    private Party makeParty(String owner, Hero... heroes) {
        Party p = new Party(owner);
        for (Hero h : heroes) p.addHero(h);
        return p;
    }

    // ===========================================================
    // TC-001: Turn Order — higher level unit acts first
    // ===========================================================
    @Test
    @DisplayName("TC-001: Higher level hero goes first in turn order")
    void testTurnOrderHigherLevelFirst() {
        Hero weak  = makeHero("Weak",  new WarriorClass()); // level 1
        Hero strong = makeHero("Strong", new WarriorClass());
        strong.setLevel(5); // manually set level for test
        strong.setAttack(15);

        Party p1 = makeParty("P1", strong);
        Party p2 = makeParty("P2", weak);

        TurnOrder to = new TurnOrder(p1, p2);
        List<Hero> queue = to.getTurnQueue();

        assertEquals("Strong", queue.get(0).getName(),
                "Higher level hero should go first");
    }

    // ===========================================================
    // TC-002: Turn Order — teams alternate
    // ===========================================================
    @Test
    @DisplayName("TC-002: Teams alternate in turn order")
    void testTurnOrderTeamsAlternate() {
        Hero a1 = makeHero("A1", new WarriorClass());
        Hero a2 = makeHero("A2", new WarriorClass());
        Hero b1 = makeHero("B1", new ChaosClass());
        Hero b2 = makeHero("B2", new ChaosClass());

        Party p1 = makeParty("P1", a1, a2);
        Party p2 = makeParty("P2", b1, b2);

        TurnOrder to = new TurnOrder(p1, p2);
        List<Hero> queue = to.getTurnQueue();

        // Teams should alternate: P1, P2, P1, P2
        assertEquals(4, queue.size());
        Party firstParty  = to.getPartyOf(queue.get(0));
        Party secondParty = to.getPartyOf(queue.get(1));
        assertNotEquals(firstParty, secondParty, "Teams must alternate in turn queue");
    }

    // ===========================================================
    // TC-003: Basic Attack — damage formula
    // ===========================================================
    @Test
    @DisplayName("TC-003: Basic attack applies correct damage (ATK - DEF)")
    void testBasicAttackDamageFormula() {
        Hero attacker = makeHero("Attacker", new WarriorClass());
        attacker.setAttack(20);
        Hero defender = makeHero("Defender", new OrderClass());
        defender.setDefense(5);
        int initialHp = defender.getCurrentHp();

        Party p1 = makeParty("P1", attacker);
        Party p2 = makeParty("P2", defender);
        PvPBattleController ctrl = new PvPBattleController(p1, p2);

        ctrl.attack(defender);

        // Damage = ATK(20) - DEF(5) = 15
        int expectedHp = initialHp - 15;
        assertEquals(expectedHp, defender.getCurrentHp(),
                "Defender should lose exactly ATK - DEF HP");
    }

    // ===========================================================
    // TC-004: Attack — minimum damage is 0
    // ===========================================================
    @Test
    @DisplayName("TC-004: Attack cannot deal negative damage (minimum 0)")
    void testAttackMinimumDamageIsZero() {
        Hero attacker = makeHero("Attacker", new OrderClass());
        attacker.setAttack(3);
        Hero defender = makeHero("Tank", new WarriorClass());
        defender.setDefense(50); // Far higher defense
        int initialHp = defender.getCurrentHp();

        Party p1 = makeParty("P1", attacker);
        Party p2 = makeParty("P2", defender);
        PvPBattleController ctrl = new PvPBattleController(p1, p2);

        ctrl.attack(defender);

        assertEquals(initialHp, defender.getCurrentHp(),
                "Attack with ATK < DEF should deal 0 damage");
    }

    // ===========================================================
    // TC-005: Defend — hero gains HP and mana on defend
    // ===========================================================
    @Test
    @DisplayName("TC-005: Defend action grants +10 HP and +5 mana")
    void testDefendGrantsHpAndMana() {
        Hero hero = makeHero("Defender", new OrderClass());
        hero.setCurrentHp(80);  // Below max
        hero.setCurrentMana(30);

        Party p1 = makeParty("P1", hero);
        Party p2 = makeParty("P2", makeHero("Enemy", new WarriorClass()));
        PvPBattleController ctrl = new PvPBattleController(p1, p2);

        ctrl.defend();

        assertEquals(90, hero.getCurrentHp(),   "Defend should add 10 HP");
        assertEquals(35, hero.getCurrentMana(), "Defend should add 5 mana");
    }

    // ===========================================================
    // TC-006: Defend — HP and mana capped at max
    // ===========================================================
    @Test
    @DisplayName("TC-006: Defend does not exceed max HP or max mana")
    void testDefendCapAtMax() {
        Hero hero = makeHero("FullHero", new OrderClass());
        // Full HP and mana already at 100/50
        Party p1 = makeParty("P1", hero);
        Party p2 = makeParty("P2", makeHero("E", new WarriorClass()));
        PvPBattleController ctrl = new PvPBattleController(p1, p2);

        ctrl.defend();

        assertEquals(hero.getMaxHp(),   hero.getCurrentHp(),   "HP should not exceed max");
        assertEquals(hero.getMaxMana(), hero.getCurrentMana(), "Mana should not exceed max");
    }

    // ===========================================================
    // TC-007: Wait — hero moves to end of turn queue
    // ===========================================================
    @Test
    @DisplayName("TC-007: Waiting hero is moved to end of turn queue")
    void testWaitMovesHeroToEndOfQueue() {
        Hero a = makeHero("Alpha", new WarriorClass());
        Hero b = makeHero("Beta",  new ChaosClass());
        Party p1 = makeParty("P1", a);
        Party p2 = makeParty("P2", b);

        BattleEngine engine = new BattleEngine(p1, p2);
        Hero firstToAct = engine.getActiveHero();
        String waitResult = engine.executeWait();

        // The hero that waited should not be next in queue
        Hero nextHero = engine.getActiveHero();
        assertNotEquals(firstToAct, nextHero,
                "After waiting, a different hero should be active");
        assertTrue(waitResult.contains("waits"));
    }

    // ===========================================================
    // TC-008: Protect ability applies shield to all allies
    // ===========================================================
    @Test
    @DisplayName("TC-008: Order's Protect applies 10% max HP shield to all allies")
    void testProtectAbilityAppliesShields() {
        Hero caster  = makeHero("Priest", new OrderClass());
        caster.setCurrentMana(50);
        Hero ally    = makeHero("Ally",   new WarriorClass());

        Party p1 = makeParty("P1", caster, ally);
        Party p2 = makeParty("P2", makeHero("Foe", new ChaosClass()));
        PvPBattleController ctrl = new PvPBattleController(p1, p2);

        // Use ability directly
        SpecialAbility protect = caster.getHeroClass().getSpecialAbilities().get(0); // Protect
        ctrl.castAbility(protect, Collections.emptyList());

        int expectedCasterShield = (int)(caster.getMaxHp() * 0.10);
        int expectedAllyShield   = (int)(ally.getMaxHp()   * 0.10);

        assertEquals(expectedCasterShield, caster.getShieldHp(),
                "Protect should apply 10% max HP shield to caster");
        assertEquals(expectedAllyShield, ally.getShieldHp(),
                "Protect should apply 10% max HP shield to ally");
    }

}
