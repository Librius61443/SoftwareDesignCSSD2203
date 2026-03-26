package battle;

import encounter.Encounter;
import encounter.EncounterOutcome;
import encounter.RoomContext;
import rng.Rng;
import unit.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Resolves a PvE battle encounter.
 *
 * Win: awards experience and gold to surviving heroes.<br>
 * Loss: deducts 10 % gold and 30 % of in-progress XP (no de-level).
 */
public final class BattleEncounter implements Encounter {

  private final Rng rng;
  private final EnemyFactory enemyFactory;
  private final BattleSystem battleSystem;

  public BattleEncounter(Rng rng, EnemyFactory enemyFactory, BattleSystem battleSystem) {
    this.rng          = Objects.requireNonNull(rng);
    this.enemyFactory = Objects.requireNonNull(enemyFactory);
    this.battleSystem = Objects.requireNonNull(battleSystem);
  }

  @Override
  public EncounterOutcome resolve(RoomContext ctx, Party party) {
    EnemyParty   enemies = enemyFactory.createForParty(party);
    BattleResult result  = battleSystem.fight(party, enemies);

    if (result.winner() == BattleWinner.PLAYER) {
      int totalExp  = enemies.units().stream().mapToInt(u -> 50 * u.level()).sum();
      int totalGold = enemies.units().stream().mapToInt(u -> 75 * u.level()).sum();

      party.addGold(totalGold);

      List<Hero> standing = party.heroes().stream()
          .filter(h -> h.hp() > 0)
          .collect(Collectors.toList());

      if (!standing.isEmpty()) {
        int share = totalExp / standing.size();
        standing.forEach(h -> h.gainExp(share));
      }
      return EncounterOutcome.WIN;
    } else {
      party.loseGoldPercent(10);
      party.applyLossExpPenalty(30);
      return EncounterOutcome.LOSS;
    }
  }
}
