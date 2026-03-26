package unit;

import java.util.List;

/** A group of enemy units encountered in a battle room. */
public record EnemyParty(List<EnemyUnit> units) {}
