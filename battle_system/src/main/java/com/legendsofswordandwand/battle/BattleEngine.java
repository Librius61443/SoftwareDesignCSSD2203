package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.SpecialAbility;

import java.util.ArrayList;
import java.util.List;

/**
 * Core engine for turn-based PvP battles.
 *
 * Responsibilities:
 * - Manages round and turn progression.
 * - Executes actions: Attack, Defend, Wait, Cast.
 * - Detects battle-end conditions.
 * - Accumulates a battle log.
 *
 * Design pattern: Facade — provides a simple interface over
 * TurnOrder, Hero, Party, and SpecialAbility interactions.
 */
public class BattleEngine {

    private final Party party1;
    private final Party party2;
    private final TurnOrder turnOrder;
    private final List<String> battleLog;

    private int roundNumber;
    private int currentTurnIndex;
    private boolean battleOver;
    private BattleResult result;

    // Observer support (for GUI to listen to events)
    private final List<BattleEventListener> listeners;

    public BattleEngine(Party party1, Party party2) {
        this.party1 = party1;
        this.party2 = party2;
        this.turnOrder = new TurnOrder(party1, party2);
        this.battleLog = new ArrayList<>();
        this.roundNumber = 1;
        this.currentTurnIndex = 0;
        this.battleOver = false;
        this.listeners = new ArrayList<>();

        log("=== Battle Start: " + party1.getOwnerName() + " vs " + party2.getOwnerName() + " ===");
        log("Round 1 begins.");
        notifyRoundStart(roundNumber);
    }

    // ---- Action execution methods ----

    /**
     * Execute an ATTACK action by the current active hero against the specified target.
     */
    public String executeAttack(Hero target) {
        Hero attacker = getActiveHero();
        if (!canAct(attacker)) return attacker.getName() + " cannot act this turn.";

        // Pass ONLY the raw attack value.
        // The target.takeDamage() method handles subtracting the defense
        // and applying any active shields based on your Hero.java logic.
        int rawDamage = attacker.getAttack();
        int actualDmg = target.takeDamage(rawDamage);

        String msg = attacker.getName() + " attacks " + target.getName()
                + " for " + actualDmg + " damage!"
                + (target.isAlive() ? "" : " " + target.getName() + " is defeated!");
        log(msg);
        notifyAction(attacker, BattleAction.ATTACK, msg);
        advanceTurn();
        return msg;
    }

    /**
     * Execute a DEFEND action by the current active hero.
     */
    public String executeDefend() {
        Hero hero = getActiveHero();
        if (!canAct(hero)) return hero.getName() + " cannot act this turn.";

        // The hero.defend() method inside Hero.java accurately handles setting
        // the defending flag and granting the +10 HP / +5 mana bonus.
        hero.defend();

        String msg = hero.getName() + " defends! (+10 HP, +5 mana)";
        log(msg);
        notifyAction(hero, BattleAction.DEFEND, msg);
        advanceTurn();
        return msg;
    }

    /**
     * Execute a WAIT action — hero moves to end of turn queue.
     */
    public String executeWait() {
        Hero hero = getActiveHero();
        if (!canAct(hero)) return hero.getName() + " cannot act this turn.";

        turnOrder.heroWaits(hero);
        String msg = hero.getName() + " waits.";
        log(msg);
        notifyAction(hero, BattleAction.WAIT, msg);

        // Do not advance turn index; queue has changed, fetch again
        checkRoundOver();
        return msg;
    }

    /**
     * Execute a CAST action using the specified ability and targets.
     */
    public String executeCast(SpecialAbility ability, List<Hero> targets) {
        Hero caster = getActiveHero();
        if (!canAct(caster)) return caster.getName() + " cannot act this turn.";

        // Use Hero's spendMana() method to both check for sufficient mana
        // AND deduct it at the beginning of the action.
        if (!caster.spendMana(ability.getManaCost())) {
            String failMsg = caster.getName() + " does not have enough mana to cast this spell. Select another action.";
            log(failMsg);
            // Do NOT advance turn here so the user can try again
            return failMsg;
        }

        Party casterParty = turnOrder.getPartyOf(caster);
        Party enemyParty = turnOrder.getOpposingParty(casterParty);

        String msg = ability.execute(caster,
                targets,
                casterParty.getAliveHeroes(),
                enemyParty.getAliveHeroes());
        log(msg);
        notifyAction(caster, BattleAction.CAST, msg);
        advanceTurn();
        return msg;
    }

    // ---- Turn management ----

    /** Returns the hero whose turn it currently is. */
    public Hero getActiveHero() {
        List<Hero> queue = turnOrder.getTurnQueue();
        if (currentTurnIndex < queue.size()) {
            return queue.get(currentTurnIndex);
        }
        return null;
    }

    /** True if the hero can currently act (not stunned, alive). */
    private boolean canAct(Hero hero) {
        if (hero == null) return false;
        if (!hero.isAlive()) {
            log(hero.getName() + " is defeated and cannot act.");
            advanceTurn();
            return false;
        }
        if (hero.isStunned()) {
            log(hero.getName() + " is stunned and loses their turn.");
            hero.setStunned(false);
            advanceTurn();
            return false;
        }
        return true;
    }

    private void advanceTurn() {
        currentTurnIndex++;
        resetDefendingFlags();
        checkRoundOver();
    }

    /** Clear defending status after each hero acts. */
    private void resetDefendingFlags() {
        Hero active = getActiveHero();
        // Since we are iterating turns, you could clear defending flags
        // when it becomes this hero's turn again. I'm leaving this hook
        // as you designed it in your original engine.
    }

    private void checkRoundOver() {
        // Check battle over first
        if (party1.isDefeated() || party2.isDefeated()) {
            endBattle();
            return;
        }
        // If we've exhausted the turn queue, start new round
        if (currentTurnIndex >= turnOrder.getTurnQueue().size()) {
            roundNumber++;
            currentTurnIndex = 0;
            turnOrder.rebuildForNewRound();
            log("--- Round " + roundNumber + " begins ---");
            notifyRoundStart(roundNumber);
        }
    }

    private void endBattle() {
        battleOver = true;
        Party winner = party1.isDefeated() ? party2 : party1;
        Party loser = party1.isDefeated() ? party1 : party2;
        String winMsg = "=== " + winner.getOwnerName() + " WINS the battle! ===";
        log(winMsg);
        result = new BattleResult(winner, loser,
                winner.getAliveHeroes(),
                loser.getAliveHeroes(),
                new ArrayList<>(battleLog));
        notifyBattleEnd(result);
    }

    // ---- Observer / Listener ----

    public void addListener(BattleEventListener listener) {
        listeners.add(listener);
    }

    private void notifyRoundStart(int round) {
        for (BattleEventListener l : listeners) l.onRoundStart(round, turnOrder.getTurnQueue());
    }

    private void notifyAction(Hero hero, BattleAction action, String message) {
        for (BattleEventListener l : listeners) l.onAction(hero, action, message);
    }

    private void notifyBattleEnd(BattleResult result) {
        for (BattleEventListener l : listeners) l.onBattleEnd(result);
    }

    // ---- Logging ----

    private void log(String message) {
        battleLog.add(message);
    }

    // ---- Getters ----

    public boolean isBattleOver() { return battleOver; }
    public BattleResult getResult() { return result; }
    public List<String> getBattleLog() { return battleLog; }
    public TurnOrder getTurnOrder() { return turnOrder; }
    public int getRoundNumber() { return roundNumber; }
    public Party getParty1() { return party1; }
    public Party getParty2() { return party2; }
}