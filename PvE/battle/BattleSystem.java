package battle;

import rng.Rng;
import unit.*;

import java.util.*;

/**
 * Runs battle rounds via {@link TurnEngine} until all units on one side are
 * dead. Dead heroes remain dead until an Inn restores them.
 */
public final class BattleSystem {

  private final Rng rng;

  public BattleSystem(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public BattleResult fight(Party party, EnemyParty enemies) {
    List<Unit> players   = new ArrayList<>(party.heroes());
    List<Unit> enemyList = new ArrayList<>(enemies.units());
    TurnEngine engine    = new TurnEngine(rng);

    while (anyAlive(players) && anyAlive(enemyList)) {
      engine.playRound(players, enemyList);
    }

    BattleWinner winner = anyAlive(players) ? BattleWinner.PLAYER : BattleWinner.ENEMY;
    return new BattleResult(winner);
  }

  private boolean anyAlive(List<Unit> units) {
    return units.stream().anyMatch(u -> u.hp() > 0);
  }
}

// ---------------------------------------------------------------------------
// Supporting types — package-private (only used within battle package)
// ---------------------------------------------------------------------------

/** Which side won a battle. */
enum BattleWinner { PLAYER, ENEMY }

/** Outcome of a single fight. */
record BattleResult(BattleWinner winner) {}

/**
 * Resolves one full round of combat.
 *
 * <p>Initiative order: highest level, then highest attack. Teams alternate.
 * Units that chose WAIT act last in FIFO order.
 */
final class TurnEngine {

  private final Rng rng;

  public TurnEngine(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public void playRound(List<Unit> players, List<Unit> enemies) {
    List<Unit> all = new ArrayList<>();
    players.stream().filter(u -> u.hp() > 0).forEach(all::add);
    enemies.stream().filter(u -> u.hp() > 0).forEach(all::add);

    all.sort(Comparator
        .comparingInt(Unit::level).reversed()
        .thenComparingInt(Unit::attack).reversed());

    if (all.isEmpty()) return;

    Team        nextTeam  = all.get(0).team();
    Queue<Unit> waitQueue = new ArrayDeque<>();
    Set<Unit>   acted     = new HashSet<>();

    // Main phase — alternating teams
    while (acted.size() < all.size()) {
      Unit actor = pickNextActor(all, acted, nextTeam);
      if (actor == null) break;

      if (actor.stunned()) {
        actor.clearStun();
        acted.add(actor);
        nextTeam = nextTeam.other();
        continue;
      }

      Action action = actor.chooseAction(players, enemies, waitQueue);
      if (action.type() == ActionType.WAIT) {
        waitQueue.add(actor);
      } else {
        action.execute(actor, players, enemies);
      }
      acted.add(actor);
      nextTeam = nextTeam.other();
    }

    // Wait phase — FIFO
    while (!waitQueue.isEmpty()) {
      Unit actor = waitQueue.poll();
      if (actor.hp() <= 0) continue;
      if (actor.stunned()) { actor.clearStun(); continue; }

      Action action = actor.chooseAction(players, enemies, new ArrayDeque<>());
      if (action.type() != ActionType.WAIT) {
        action.execute(actor, players, enemies);
      }
    }
  }

  private Unit pickNextActor(List<Unit> ordered, Set<Unit> acted, Team team) {
    for (Unit u : ordered) {
      if (!acted.contains(u) && u.team() == team) return u;
    }
    for (Unit u : ordered) {
      if (!acted.contains(u)) return u;
    }
    return null;
  }
}
