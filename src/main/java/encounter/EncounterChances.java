package encounter;

/**
 * Computed encounter chances for a single room.
 *
 * @param battlePercent Probability (0–100) that the room is a battle.
 * @param innPercent    Probability (0–100) that the room is an inn.
 */
public record EncounterChances(int battlePercent, int innPercent) {}
