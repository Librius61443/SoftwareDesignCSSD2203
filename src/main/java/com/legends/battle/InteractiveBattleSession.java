package com.legends.battle;

import unit.Action;
import unit.ActionType;
import unit.CombatRules;
import unit.EnemyParty;
import unit.Hero;
import unit.HeroAbility;
import unit.Party;
import unit.Team;
import unit.Unit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class InteractiveBattleSession {

    public record BattleOutcome(Team winner) {}

    private final List<Unit> players;
    private final List<Unit> enemies;
    private final EnumSet<Team> controlledTeams;
    private final List<String> battleLog = new ArrayList<>();

    private List<Unit> currentQueue = new ArrayList<>();
    private final Queue<Unit> waitQueue = new ArrayDeque<>();
    private boolean inWaitPhase;
    private int roundNumber = 0;
    private int currentIndex = 0;
    private BattleOutcome outcome;

    public InteractiveBattleSession(Party playerParty, EnemyParty enemyParty) {
        this(new ArrayList<>(playerParty.heroes()), new ArrayList<>(enemyParty.units()), EnumSet.of(Team.PLAYER));
    }

    public InteractiveBattleSession(Party leftParty, Party rightParty, EnumSet<Team> controlledTeams) {
        this(
            leftParty.heroes().stream().map(hero -> hero.copyForTeam(Team.PLAYER)).map(Unit.class::cast).toList(),
            rightParty.heroes().stream().map(hero -> hero.copyForTeam(Team.ENEMY)).map(Unit.class::cast).toList(),
            controlledTeams
        );
    }

    private InteractiveBattleSession(List<Unit> players, List<Unit> enemies, EnumSet<Team> controlledTeams) {
        this.players = new ArrayList<>(players);
        this.enemies = new ArrayList<>(enemies);
        this.controlledTeams = controlledTeams.clone();
        startRound();
        advanceUntilControlledTurn();
    }

    public int roundNumber() {
        return roundNumber;
    }

    public boolean isBattleOver() {
        return outcome != null;
    }

    public BattleOutcome outcome() {
        return outcome;
    }

    public Unit currentActor() {
        if (isBattleOver() || currentIndex >= currentQueue.size()) {
            return null;
        }
        return currentQueue.get(currentIndex);
    }

    public List<Unit> players() {
        return players;
    }

    public List<Unit> enemies() {
        return enemies;
    }

    public List<String> battleLog() {
        return battleLog;
    }

    public List<HeroAbility> availableAbilities() {
        Unit actor = currentActor();
        if (!(actor instanceof Hero hero)) {
            return List.of();
        }
        return switch (hero.currentClass()) {
            case ORDER -> List.of(HeroAbility.PROTECT, HeroAbility.HEAL);
            case CHAOS -> List.of(HeroAbility.FIREBALL, HeroAbility.CHAIN_LIGHTNING);
            case WARRIOR -> List.of(HeroAbility.BERSERKER_ATTACK);
            case MAGE -> List.of(HeroAbility.REPLENISH);
        };
    }

    public boolean canWait() {
        return !inWaitPhase && currentActor() != null;
    }

    public void attack(Unit target) {
        Unit actor = requireControlledActor();
        if (target == null || !target.isAlive()) {
            throw new IllegalArgumentException("Choose a living target.");
        }
        int damage = CombatRules.performAttack(actor, target, actor.team() == Team.PLAYER ? enemies : players);
        log(actor.name() + " attacks " + target.name() + " for " + damage + " damage.");
        endTurn();
    }

    public void defend() {
        Unit actor = requireControlledActor();
        actor.heal(10);
        actor.restoreMana(5);
        log(actor.name() + " defends and recovers 10 HP and 5 mana.");
        endTurn();
    }

    public void waitTurn() {
        Unit actor = requireControlledActor();
        if (inWaitPhase) {
            throw new IllegalStateException("Cannot wait again during the wait phase.");
        }
        waitQueue.add(actor);
        log(actor.name() + " waits until the end of the round.");
        currentIndex++;
        advanceUntilControlledTurn();
    }

    public void cast(HeroAbility ability, List<Unit> selectedTargets) {
        Unit actor = requireControlledActor();
        if (!(actor instanceof Hero hero)) {
            throw new IllegalStateException("Only heroes can cast abilities.");
        }
        int manaBefore = hero.mana();
        List<Unit> allies = actor.team() == Team.PLAYER ? players : enemies;
        List<Unit> foes = actor.team() == Team.PLAYER ? enemies : players;
        ability.execute(hero, allies, foes, selectedTargets == null ? List.of() : selectedTargets);
        if (hero.mana() == manaBefore) {
            throw new IllegalStateException("Not enough mana to cast " + ability.name() + ".");
        }
        log(hero.name() + " casts " + prettify(ability.name()) + ".");
        endTurn();
    }

    private Unit requireControlledActor() {
        Unit actor = currentActor();
        if (actor == null || isBattleOver()) {
            throw new IllegalStateException("No active turn.");
        }
        if (!controlledTeams.contains(actor.team())) {
            throw new IllegalStateException("Current actor is not player controlled.");
        }
        return actor;
    }

    private void endTurn() {
        currentIndex++;
        advanceUntilControlledTurn();
    }

    private void advanceUntilControlledTurn() {
        while (!isBattleOver()) {
            if (!anyAlive(players)) {
                outcome = new BattleOutcome(Team.ENEMY);
                log("Battle over. Enemy team wins.");
                return;
            }
            if (!anyAlive(enemies)) {
                outcome = new BattleOutcome(Team.PLAYER);
                log("Battle over. Player team wins.");
                return;
            }

            if (currentIndex >= currentQueue.size()) {
                if (!inWaitPhase && !waitQueue.isEmpty()) {
                    currentQueue = new ArrayList<>(waitQueue);
                    waitQueue.clear();
                    currentIndex = 0;
                    inWaitPhase = true;
                    continue;
                }
                startRound();
                continue;
            }

            Unit actor = currentQueue.get(currentIndex);
            if (!actor.isAlive()) {
                currentIndex++;
                continue;
            }
            if (actor.stunned()) {
                actor.clearStun();
                log(actor.name() + " is stunned and loses the turn.");
                currentIndex++;
                continue;
            }
            if (controlledTeams.contains(actor.team())) {
                return;
            }
            runAiTurn(actor);
        }
    }

    private void runAiTurn(Unit actor) {
        Action action = actor.chooseAction(players, enemies, new ArrayDeque<>());
        if (action.type() == ActionType.WAIT && !inWaitPhase) {
            waitQueue.add(actor);
            log(actor.name() + " waits.");
            currentIndex++;
            return;
        }
        logAiAction(actor, action);
        if (action.type() == ActionType.WAIT) {
            applyAutoAttack(actor);
        } else {
            action.execute(actor, players, enemies);
        }
        currentIndex++;
    }

    private void logAiAction(Unit actor, Action action) {
        if (action.type() == ActionType.CAST) {
            log(actor.name() + " casts a special ability.");
        } else if (action.type() == ActionType.WAIT) {
            log(actor.name() + " waits.");
        } else if (action.type() == ActionType.DEFEND) {
            log(actor.name() + " defends.");
        } else {
            Unit target = lowestHpAlive(actor.team() == Team.PLAYER ? enemies : players);
            if (target != null) {
                int damage = CombatRules.basicDamage(actor, target);
                log(actor.name() + " attacks " + target.name() + " for " + damage + " damage.");
            }
        }
    }

    private void startRound() {
        roundNumber++;
        inWaitPhase = false;
        currentIndex = 0;
        waitQueue.clear();
        currentQueue = buildRoundOrder();
        log("Round " + roundNumber + " begins.");
    }

    private List<Unit> buildRoundOrder() {
        List<Unit> all = new ArrayList<>();
        players.stream().filter(Unit::isAlive).forEach(all::add);
        enemies.stream().filter(Unit::isAlive).forEach(all::add);
        all.sort(Comparator.comparingInt(Unit::level).reversed().thenComparingInt(Unit::attack).reversed());
        if (all.isEmpty()) {
            return List.of();
        }

        Team nextTeam = all.get(0).team();
        Set<Unit> used = new HashSet<>();
        List<Unit> ordered = new ArrayList<>();
        while (ordered.size() < all.size()) {
            Unit next = pickNextActor(all, used, nextTeam);
            if (next == null) {
                break;
            }
            ordered.add(next);
            used.add(next);
            nextTeam = nextTeam.other();
        }
        return ordered;
    }

    private Unit pickNextActor(List<Unit> ordered, Set<Unit> used, Team team) {
        for (Unit unit : ordered) {
            if (!used.contains(unit) && unit.team() == team) {
                return unit;
            }
        }
        for (Unit unit : ordered) {
            if (!used.contains(unit)) {
                return unit;
            }
        }
        return null;
    }

    private Unit lowestHpAlive(List<Unit> units) {
        return units.stream().filter(Unit::isAlive).min(Comparator.comparingInt(Unit::hp)).orElse(null);
    }

    private boolean anyAlive(List<Unit> units) {
        return units.stream().anyMatch(Unit::isAlive);
    }

    private void applyAutoAttack(Unit actor) {
        Unit target = lowestHpAlive(actor.team() == Team.PLAYER ? enemies : players);
        if (target != null) {
            CombatRules.performAttack(actor, target, actor.team() == Team.PLAYER ? enemies : players);
        }
    }

    private void log(String message) {
        battleLog.add(message);
    }

    private String prettify(String name) {
        return name.toLowerCase().replace('_', ' ');
    }
}
