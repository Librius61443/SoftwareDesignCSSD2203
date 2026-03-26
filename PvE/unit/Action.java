package unit;

import java.util.Comparator;
import java.util.List;

/** A combat action that can be chosen and executed by any {@link Unit}. */
public interface Action {
  ActionType type();
  void execute(Unit actor, List<Unit> players, List<Unit> enemies);
}


/** Attacks the opposing unit with the lowest current HP. */
class AttackAction implements Action {

  @Override public ActionType type() { return ActionType.ATTACK; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    List<Unit> foes   = actor.team() == Team.PLAYER ? enemies : players;
    Unit       target = lowestHpAlive(foes);
    if (target == null) return;
    target.takeDamage(Math.max(0, actor.attack() - target.defense()));
  }

  private Unit lowestHpAlive(List<Unit> units) {
    return units.stream()
        .filter(u -> u.hp() > 0)
        .min(Comparator.comparingInt(Unit::hp))
        .orElse(null);
  }
}


/** Restores 10 HP and 5 mana to the acting unit. */
class DefendAction implements Action {

  @Override public ActionType type() { return ActionType.DEFEND; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    actor.heal(10);
    actor.restoreMana(5);
  }
}


/** Placeholder; unit acts at the end of the round via the wait queue. */
class WaitAction implements Action {

  @Override public ActionType type() { return ActionType.WAIT; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    // Resolved by TurnEngine after all non-waiting units have acted.
  }
}
