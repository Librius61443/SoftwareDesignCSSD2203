package com.legends.controller;

import com.legends.battle.InteractiveBattleSession;
import com.legends.dao.UserDAO;
import com.legends.model.User;
import com.legends.service.AppServices;
import com.legends.service.MatchmakingService;
import com.legends.view.PvpBattleView;
import unit.HeroAbility;
import unit.Party;
import unit.Team;
import unit.Unit;

import java.util.EnumSet;
import java.util.List;

/**
 * Drives an interactive PvP battle where both players control their own heroes.
 *
 * Each player has their own PvpBattleView window. On every turn:
 *  - The window of the player whose hero is acting has buttons enabled.
 *  - The other window shows "Opponent's turn…" with buttons disabled.
 *
 * Player 1 always maps to Team.PLAYER, Player 2 to Team.ENEMY.
 */
public class PvpBattleController {

    private final InteractiveBattleSession session;
    private final PvpBattleView view1;   // challenger — controls PLAYER team
    private final PvpBattleView view2;   // opponent   — controls ENEMY  team

    private final String challengerName;
    private final String opponentName;
    private final MatchmakingService matchmakingService;

    public PvpBattleController(Party challengerParty, Party opponentParty,
                                String challengerName, String opponentName,
                                int challenger_x, int opponent_x,
                                MatchmakingService matchmakingService) {
        this.challengerName      = challengerName;
        this.opponentName        = opponentName;
        this.matchmakingService  = matchmakingService;

        // Both teams are human-controlled
        this.session = new InteractiveBattleSession(
            challengerParty, opponentParty, EnumSet.of(Team.PLAYER, Team.ENEMY)
        );

        this.view1 = new PvpBattleView(challengerName, challenger_x);
        this.view2 = new PvpBattleView(opponentName,   opponent_x);

        wireView(view1, Team.PLAYER);
        wireView(view2, Team.ENEMY);

        refresh();

        view1.setVisible(true);
        view2.setVisible(true);
    }

    // ── Wire one view to the session ─────────────────────────────────────────

    private void wireView(PvpBattleView view, Team controlledTeam) {
        view.addAttackListener(e -> handleAttack(view, controlledTeam));
        view.addDefendListener(e -> handleDefend(view, controlledTeam));
        view.addWaitListener  (e -> handleWait  (view, controlledTeam));
        view.addCastListener  (e -> handleCast  (view, controlledTeam));
    }

    // ── Action handlers ───────────────────────────────────────────────────────

    private void handleAttack(PvpBattleView view, Team controlledTeam) {
        if (!isMyTurn(controlledTeam)) return;
        try {
            int[] indices = view.selectedEnemyIndices();
            if (indices.length == 0) throw new IllegalArgumentException("Select a target first.");
            List<Unit> foes = foesFor(controlledTeam);
            session.attack(foes.get(indices[0]));
            onActionDone();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleDefend(PvpBattleView view, Team controlledTeam) {
        if (!isMyTurn(controlledTeam)) return;
        try {
            session.defend();
            onActionDone();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleWait(PvpBattleView view, Team controlledTeam) {
        if (!isMyTurn(controlledTeam)) return;
        try {
            session.waitTurn();
            onActionDone();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleCast(PvpBattleView view, Team controlledTeam) {
        if (!isMyTurn(controlledTeam)) return;
        try {
            HeroAbility ability = view.selectedAbility();
            if (ability == null) throw new IllegalArgumentException("Choose an ability first.");
            session.cast(ability, selectedTargets(view, ability, controlledTeam));
            onActionDone();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    // ── After every action ────────────────────────────────────────────────────

    private void onActionDone() {
        if (session.isBattleOver()) {
            refresh();
            Team winnerTeam = session.outcome().winner();
            String winner = winnerTeam == Team.PLAYER ? challengerName : opponentName;
            String loser  = winnerTeam == Team.PLAYER ? opponentName   : challengerName;

            updateLeagueStats(winner, loser);
            String summary = winner + " defeated " + loser + " in PvP!";
            view1.showResult(summary);
            view2.showResult(summary);
            matchmakingService.notifyMatchCompleted(challengerName, opponentName, summary);
            return;
        }
        refresh();
    }

    // ── Refresh both windows ──────────────────────────────────────────────────

    private void refresh() {
        Unit actor      = session.currentActor();
        boolean p1Turn  = actor != null && actor.team() == Team.PLAYER;
        boolean p2Turn  = actor != null && actor.team() == Team.ENEMY;

        // View 1: allies = PLAYER team, enemies = ENEMY team
        view1.render(
            session.roundNumber(), actor,
            session.players(), session.enemies(),
            p1Turn ? session.availableAbilities() : List.of(),
            session.battleLog(), session.canWait(), p1Turn
        );

        // View 2: allies = ENEMY team, enemies = PLAYER team (flipped perspective)
        view2.render(
            session.roundNumber(), actor,
            session.enemies(), session.players(),
            p2Turn ? session.availableAbilities() : List.of(),
            session.battleLog(), session.canWait(), p2Turn
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isMyTurn(Team controlledTeam) {
        Unit actor = session.currentActor();
        return actor != null && actor.team() == controlledTeam;
    }

    private List<Unit> foesFor(Team controlledTeam) {
        return controlledTeam == Team.PLAYER ? session.enemies() : session.players();
    }

    private List<Unit> alliesFor(Team controlledTeam) {
        return controlledTeam == Team.PLAYER ? session.players() : session.enemies();
    }

    private List<Unit> selectedTargets(PvpBattleView view, HeroAbility ability, Team controlledTeam) {
        return switch (ability) {
            case FIREBALL, CHAIN_LIGHTNING, BERSERKER_ATTACK -> {
                List<Unit> foes = foesFor(controlledTeam);
                yield List.of(foes.get(Math.max(0, view.selectedEnemyIndices().length > 0
                    ? view.selectedEnemyIndices()[0] : 0)));
            }
            case PROTECT, HEAL, REPLENISH -> {
                List<Unit> allies = alliesFor(controlledTeam);
                yield List.of(allies.get(Math.max(0, view.selectedAllyIndices().length > 0
                    ? view.selectedAllyIndices()[0] : 0)));
            }
        };
    }

    private void updateLeagueStats(String winnerUsername, String loserUsername) {
        UserDAO userDAO = AppServices.userDAO();
        User winner = userDAO.getUser(winnerUsername);
        if (winner != null) { winner.setWins(winner.getWins() + 1); userDAO.updateStats(winner); }
        User loser = userDAO.getUser(loserUsername);
        if (loser != null) { loser.setLosses(loser.getLosses() + 1); userDAO.updateStats(loser); }
    }
}
