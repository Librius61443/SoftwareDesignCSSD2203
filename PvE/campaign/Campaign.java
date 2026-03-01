package campaign;

import encounter.*;
import unit.Party;

import java.util.Objects;

/**
 * Drives the 30-room dungeon campaign loop.
 *
 * <p>Each room resolves one {@link Encounter}. On a loss the party is sent
 * back to the last inn (simulated by a full restore; gold/XP penalties are
 * applied inside BattleEncounter).
 */
public final class Campaign {

  private final GameConfig            cfg;
  private final EncounterChancePolicy chancePolicy;
  private final EncounterFactory      encounterFactory;

  public Campaign(GameConfig cfg, EncounterChancePolicy chancePolicy,
                  EncounterFactory encounterFactory) {
    this.cfg              = Objects.requireNonNull(cfg);
    this.chancePolicy     = Objects.requireNonNull(chancePolicy);
    this.encounterFactory = Objects.requireNonNull(encounterFactory);
  }

  public CampaignResult run(Party party) {
    Objects.requireNonNull(party);

    for (int roomIndex = 1; roomIndex <= cfg.rooms(); roomIndex++) {
      EncounterChances chances   = chancePolicy.chancesFor(party);
      Encounter        encounter = encounterFactory.createEncounter(chances);
      EncounterOutcome outcome   = encounter.resolve(new RoomContext(roomIndex), party);

      if (outcome == EncounterOutcome.LOSS) {
        party.fullRestore();
      }
    }

    return new CampaignResult(cfg.rooms(), party);
  }
}
