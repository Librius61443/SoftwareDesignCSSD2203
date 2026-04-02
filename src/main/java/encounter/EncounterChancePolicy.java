package encounter;

import unit.Party;

/**
 * Determines battle vs inn probability based on the party's cumulative level.
 *
 * Battle chance starts at 60 % and increases by 3 % per 10 cumulative hero
 * levels, capped at 90 % (inn is always at least 10 %).
 */
public final class EncounterChancePolicy {

  public EncounterChances chancesFor(Party party) {
    int levels = party.cumulativeLevel();
    int battle = clamp(60 + ((levels / 10) * 3), 60, 90);
    return new EncounterChances(battle, 100 - battle);
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
