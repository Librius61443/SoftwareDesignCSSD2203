package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.SpecialAbility;

import java.util.Collections;
import java.util.List;

/**
 * High-level controller for a PvP (Player vs Player) battle session.
 *
 * This class acts as a Controller in MVC — it mediates between the GUI
 * and the BattleEngine. The GUI calls methods here; the controller
 * delegates to BattleEngine and fires back updates via BattleEventListener.
 *
 * Per the spec: "When battling other player parties, no experience or gold is gained."
 * PvP battle uses the same battle mechanics as PvE (identical BattleEngine).
 */
public class PvPBattleController {

    private final BattleEngine engine;

    public PvPBattleController(Party player1Party, Party player2Party) {
        this.engine = new BattleEngine(player1Party, player2Party);
    }

    /** Register a listener (e.g., GUI) to receive battle events. */
    public void addListener(BattleEventListener listener) {
        engine.addListener(listener);
    }

    /** Returns the hero whose turn it currently is. */
    public Hero getActiveHero() {
        return engine.getActiveHero();
    }

    /** Returns the party the given hero belongs to. */
    public Party getPartyOf(Hero hero) {
        return engine.getTurnOrder().getPartyOf(hero);
    }

    /** Returns the opponents of the given hero. */
    public List<Hero> getEnemiesOf(Hero hero) {
        Party party = getPartyOf(hero);
        if (party == null) return Collections.emptyList();
        return engine.getTurnOrder().getOpposingParty(party).getAliveHeroes();
    }

    /** Returns the allies of the given hero (including self). */
    public List<Hero> getAlliesOf(Hero hero) {
        Party party = getPartyOf(hero);
        if (party == null) return Collections.emptyList();
        return party.getAliveHeroes();
    }

    /** Player chooses to attack a specific target. */
    public String attack(Hero target) {
        if (engine.isBattleOver()) return "Battle is already over.";
        return engine.executeAttack(target);
    }

    /** Player chooses to defend. */
    public String defend() {
        if (engine.isBattleOver()) return "Battle is already over.";
        return engine.executeDefend();
    }

    /** Player chooses to wait. */
    public String wait_() {
        if (engine.isBattleOver()) return "Battle is already over.";
        return engine.executeWait();
    }

    /** Player chooses to cast an ability. */
    public String castAbility(SpecialAbility ability, List<Hero> targets) {
        if (engine.isBattleOver()) return "Battle is already over.";
        return engine.executeCast(ability, targets);
    }

    /** Check if the battle is over. */
    public boolean isBattleOver() {
        return engine.isBattleOver();
    }

    /** Get the battle result (null if not over). */
    public BattleResult getResult() {
        return engine.getResult();
    }

    /** Full battle log. */
    public List<String> getBattleLog() {
        return engine.getBattleLog();
    }

    /** Current round number. */
    public int getRoundNumber() {
        return engine.getRoundNumber();
    }

    /** The ordered list of heroes for the current round. */
    public List<Hero> getCurrentTurnOrder() {
        return engine.getTurnOrder().getTurnQueue();
    }

    public Party getParty1() { return engine.getParty1(); }
    public Party getParty2() { return engine.getParty2(); }
}
