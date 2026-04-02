package battle;

import rng.Rng;
import unit.*;

import java.util.*;

/**
 * Runs battle rounds via {@link TurnEngine} until all units on one side are
 * dead. Dead heroes remain dead until an Inn restores them.
 */
public final class BattleSystem {

  private static final int MAX_ROUNDS = 500;

  private final Rng rng;

  public BattleSystem(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public BattleResult fight(Party party, EnemyParty enemies) {
    List<Unit> players   = new ArrayList<>(party.heroes());
    List<Unit> enemyList = new ArrayList<>(enemies.units());
    return fightUnits(players, enemyList);
  }

  public BattleResult fight(Party challenger, Party opponent) {
    List<Unit> players = new ArrayList<>();
    for (Hero hero : challenger.heroes()) {
      players.add(hero.copyForTeam(Team.PLAYER));
    }
    List<Unit> enemies = new ArrayList<>();
    for (Hero hero : opponent.heroes()) {
      enemies.add(hero.copyForTeam(Team.ENEMY));
    }
    return fightUnits(players, enemies);
  }

  public boolean doesFirstPartyWin(Party challenger, Party opponent) {
    return fight(challenger, opponent).winner() == BattleWinner.PLAYER;
  }

  private BattleResult fightUnits(List<Unit> players, List<Unit> enemyList) {
    TurnEngine engine    = new TurnEngine(rng);
    int rounds = 0;

    while (anyAlive(players) && anyAlive(enemyList) && rounds < MAX_ROUNDS) {
      engine.playRound(players, enemyList);
      rounds++;
    }

    BattleWinner winner;
    if (anyAlive(players) && !anyAlive(enemyList)) {
      winner = BattleWinner.PLAYER;
    } else if (!anyAlive(players) && anyAlive(enemyList)) {
      winner = BattleWinner.ENEMY;
    } else {
      winner = totalHp(players) >= totalHp(enemyList) ? BattleWinner.PLAYER : BattleWinner.ENEMY;
    }
    return new BattleResult(winner);
  }

  private boolean anyAlive(List<Unit> units) {
    return units.stream().anyMatch(u -> u.hp() > 0);
  }

  private int totalHp(List<Unit> units) {
    return units.stream().mapToInt(Unit::hp).sum();
  }
}

final class LocalAttackAction implements Action {

  @Override public ActionType type() { return ActionType.ATTACK; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    List<Unit> foes = actor.team() == Team.PLAYER ? enemies : players;
    Unit target = foes.stream()
        .filter(u -> u.hp() > 0)
        .min(Comparator.comparingInt(Unit::hp))
        .orElse(null);
    if (target != null) {
      CombatRules.performAttack(actor, target, foes);
    }
  }
}

final class LocalDefendAction implements Action {

  @Override public ActionType type() { return ActionType.DEFEND; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    actor.heal(10);
    actor.restoreMana(5);
  }
}

final class LocalWaitAction implements Action {

  @Override public ActionType type() { return ActionType.WAIT; }

  @Override
  public void execute(Unit actor, List<Unit> players, List<Unit> enemies) {
    // TurnEngine resolves waiting units at the end of the round.
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
