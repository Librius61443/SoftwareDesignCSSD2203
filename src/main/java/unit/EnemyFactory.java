package unit;

import rng.Rng;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generates a scaled {@link EnemyParty} appropriate for the player's
 * current cumulative level.
 *
 * <p>Party size: 1–5 units. Cumulative enemy level is randomly chosen
 * within ±10 of the player's cumulative level.
 */
public final class EnemyFactory {

  private final Rng rng;

  public EnemyFactory(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public EnemyParty createForParty(Party party) {
    int nUnits    = rng.nextInt(1, 5);
    int partyLvl  = party.cumulativeLevel();
    // Scale enemy cumulative level with party level per spec
    // (enemy party should be in the same rough range as the player)
    int minCum    = Math.max(nUnits, partyLvl - 10);
    int targetCum = rng.nextInt(minCum, Math.max(minCum, partyLvl));

    List<Integer>   levels = splitIntoLevels(nUnits, targetCum);
    List<EnemyUnit> units  = new ArrayList<>();

    for (int i = 0; i < nUnits; i++) {
      int L = clamp(levels.get(i), 1, 10);
      units.add(new EnemyUnit(
          "Enemy-" + (i + 1), L,
          4 + L,           // attack
          2 + L,           // defense
          80 + (L * 12),   // maxHp
          40  + (L * 4)    // maxMana
      ));
    }
    return new EnemyParty(units);
  }

  /** Splits {@code sum} into {@code parts} positive integers summing to roughly {@code sum}. */
  private List<Integer> splitIntoLevels(int parts, int sum) {
    List<Integer> out       = new ArrayList<>();
    int           remaining = Math.max(parts, sum);
    for (int i = 0; i < parts; i++) {
      int left  = parts - i;
      int chunk = (i == parts - 1)
          ? remaining
          : rng.nextInt(1, Math.max(1, remaining - (left - 1)));
      out.add(chunk);
      remaining -= chunk;
    }
    return out;
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
