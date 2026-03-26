package com.legends.controller;

import com.legends.battle.InteractiveBattleSession;
import com.legends.view.BattleView;
import unit.HeroAbility;
import unit.Unit;

import java.util.ArrayList;
import java.util.List;

public class BattleController {

    private final InteractiveBattleSession session;
    private final BattleView view;

    public BattleController(InteractiveBattleSession session, BattleView view) {
        this.session = session;
        this.view = view;
        this.view.addAttackListener(e -> handleAttack());
        this.view.addDefendListener(e -> handleDefend());
        this.view.addWaitListener(e -> handleWait());
        this.view.addCastListener(e -> handleCast());
        refresh();
    }

    public InteractiveBattleSession.BattleOutcome runBattle() {
        view.setVisible(true);
        return session.outcome();
    }

    private void handleAttack() {
        try {
            int[] indices = view.selectedEnemyIndices();
            Unit actor = session.currentActor();
            List<Unit> enemies = actor != null && actor.team() == unit.Team.PLAYER ? session.enemies() : session.players();
            if (indices.length == 0) {
                throw new IllegalArgumentException("Select an enemy target.");
            }
            session.attack(enemies.get(indices[0]));
            onActionComplete();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleDefend() {
        try {
            session.defend();
            onActionComplete();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleWait() {
        try {
            session.waitTurn();
            onActionComplete();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleCast() {
        try {
            HeroAbility ability = view.selectedAbility();
            if (ability == null) {
                throw new IllegalArgumentException("Choose an ability first.");
            }
            session.cast(ability, selectedTargets(ability));
            onActionComplete();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private List<Unit> selectedTargets(HeroAbility ability) {
        List<Unit> targets = new ArrayList<>();
        switch (ability) {
            case FIREBALL, CHAIN_LIGHTNING, BERSERKER_ATTACK -> {
                List<Unit> enemies = session.currentActor() != null && session.currentActor().team() == unit.Team.PLAYER
                    ? session.enemies() : session.players();
                for (int index : view.selectedEnemyIndices()) {
                    if (index >= 0 && index < enemies.size()) {
                        targets.add(enemies.get(index));
                    }
                }
            }
            case PROTECT, HEAL, REPLENISH -> {
                List<Unit> allies = session.currentActor() != null && session.currentActor().team() == unit.Team.PLAYER
                    ? session.players() : session.enemies();
                for (int index : view.selectedAllyIndices()) {
                    if (index >= 0 && index < allies.size()) {
                        targets.add(allies.get(index));
                    }
                }
            }
        }
        return targets;
    }

    private void onActionComplete() {
        if (session.isBattleOver()) {
            refresh();
            view.showInfo("Winner: " + session.outcome().winner());
            view.dispose();
            return;
        }
        refresh();
    }

    private void refresh() {
        Unit actor = session.currentActor();
        List<Unit> allies = actor != null && actor.team() == unit.Team.ENEMY ? session.enemies() : session.players();
        List<Unit> enemies = actor != null && actor.team() == unit.Team.ENEMY ? session.players() : session.enemies();
        view.render(session.roundNumber(), actor, allies, enemies, session.availableAbilities(), session.battleLog(), session.canWait());
    }
}
