package com.legends.service;

import battle.BattleSystem;
import com.legends.dao.UserDAO;
import com.legends.model.User;
import rng.JavaRng;
import unit.Party;

import java.util.Random;

/**
 * Resolves PvP matches between saved parties produced by PvE campaigns.
 */
public class PvpBattleService {

    public record PvpBattleResult(String challenger, String opponent, String winner, String loser) {}

    private final PartyService partyService;
    private final UserDAO userDAO;
    private final BattleSystem battleSystem;

    public PvpBattleService(PartyService partyService, UserDAO userDAO) {
        this.partyService = partyService;
        this.userDAO = userDAO;
        this.battleSystem = new BattleSystem(new JavaRng(new Random()));
    }

    public PvpBattleResult battleFirstSavedParties(String challenger, String opponent) {
        Party challengerParty = partyService.getSavedParty(challenger, 0);
        Party opponentParty = partyService.getSavedParty(opponent, 0);

        if (challengerParty == null || opponentParty == null) {
            throw new IllegalStateException("Both players need at least one saved PvE party before PvP.");
        }

        boolean challengerWon = battleSystem.doesFirstPartyWin(challengerParty.copy(), opponentParty.copy());
        String winner = challengerWon ? challenger : opponent;
        String loser = challengerWon ? opponent : challenger;

        updateLeagueStats(winner, loser);
        return new PvpBattleResult(challenger, opponent, winner, loser);
    }

    private void updateLeagueStats(String winnerUsername, String loserUsername) {
        User winner = userDAO.getUser(winnerUsername);
        if (winner != null) {
            winner.setWins(winner.getWins() + 1);
            userDAO.updateStats(winner);
        }

        User loser = userDAO.getUser(loserUsername);
        if (loser != null) {
            loser.setLosses(loser.getLosses() + 1);
            userDAO.updateStats(loser);
        }
    }
}
