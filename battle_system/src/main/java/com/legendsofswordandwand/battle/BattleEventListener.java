package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;
import java.util.List;

/**
 * Observer interface for battle events.
 * Implemented by the GUI and any other component that needs to react to battle state changes.
 *
 * Design Pattern: Observer
 */
public interface BattleEventListener {

    /** Called at the beginning of each new round. */
    void onRoundStart(int roundNumber, List<Hero> turnOrder);

    /** Called after each hero performs an action. */
    void onAction(Hero actor, BattleAction action, String logMessage);

    /** Called when the battle ends. */
    void onBattleEnd(BattleResult result);
}
