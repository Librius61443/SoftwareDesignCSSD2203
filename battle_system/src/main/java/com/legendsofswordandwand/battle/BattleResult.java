package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;

import java.util.List;

/**
 * Encapsulates the result of a completed PvP battle.
 */
public class BattleResult {

    private final Party winner;
    private final Party loser;
    private final List<Hero> survivingWinners;
    private final List<Hero> survivingLosers;
    private final List<String> battleLog;

    public BattleResult(Party winner, Party loser,
                        List<Hero> survivingWinners,
                        List<Hero> survivingLosers,
                        List<String> battleLog) {
        this.winner = winner;
        this.loser = loser;
        this.survivingWinners = survivingWinners;
        this.survivingLosers = survivingLosers;
        this.battleLog = battleLog;
    }

    public Party getWinner() { return winner; }
    public Party getLoser() { return loser; }
    public List<Hero> getSurvivingWinners() { return survivingWinners; }
    public List<Hero> getSurvivingLosers() { return survivingLosers; }
    public List<String> getBattleLog() { return battleLog; }

    @Override
    public String toString() {
        return "Battle Result: " + winner.getOwnerName() + " WINS! "
                + "Survivors: " + survivingWinners.size();
    }
}
