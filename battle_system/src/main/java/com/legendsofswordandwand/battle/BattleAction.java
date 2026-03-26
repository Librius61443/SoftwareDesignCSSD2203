package com.legendsofswordandwand.battle;

/**
 * Represents the types of actions a hero can take on their turn.
 */
public enum BattleAction {
    ATTACK,     // Basic attack against a target
    DEFEND,     // Skip turn, gain +10 HP +5 mana, reduce incoming damage
    WAIT,       // Postpone action to end of turn (FIFO queue)
    CAST        // Cast a special ability
}
