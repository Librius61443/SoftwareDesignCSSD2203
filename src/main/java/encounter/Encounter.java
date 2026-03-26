package encounter;

import unit.Party;

/** A single room event that produces an {@link EncounterOutcome}. */
public interface Encounter {
  EncounterOutcome resolve(RoomContext ctx, Party party);
}
