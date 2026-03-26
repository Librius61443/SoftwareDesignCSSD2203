package encounter;

import battle.BattleEncounter;
import battle.BattleSystem;
import rng.Rng;
import unit.EnemyFactory;

import java.util.Objects;

/**
 * Randomly decides whether a room is a battle or an inn based on the
 * {@link EncounterChances} supplied by {@link EncounterChancePolicy}.
 */
public final class EncounterFactory {

  private final Rng         rng;
  private final EnemyFactory enemyFactory;
  private final InnFactory   innFactory;

  public EncounterFactory(Rng rng, EnemyFactory enemyFactory, InnFactory innFactory) {
    this.rng          = Objects.requireNonNull(rng);
    this.enemyFactory = Objects.requireNonNull(enemyFactory);
    this.innFactory   = Objects.requireNonNull(innFactory);
  }

  public Encounter createEncounter(EncounterChances chances) {
    int roll = rng.nextInt(1, 100);
    if (roll <= chances.battlePercent()) {
      return new BattleEncounter(rng, enemyFactory, new BattleSystem(rng));
    }
    return innFactory.createInn();
  }
}
