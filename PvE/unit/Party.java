package unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** The player's party: up to 5 heroes and a gold wallet. */
public final class Party {

  private final List<Hero> heroes = new ArrayList<>();
  private int              gold   = 0;

  private Party() {}

  /** Creates a party with one hero of the given name. */
  public static Party singleStarterHero(String name) {
    Party p = new Party();
    p.heroes.add(new Hero(name));
    return p;
  }


  public List<Hero> heroes()           { return Collections.unmodifiableList(heroes); }
  public int        gold()             { return gold; }

  /** Sum of all hero levels (used for encounter-chance scaling). */
  public int cumulativeLevel() {
    return heroes.stream().mapToInt(Hero::level).sum();
  }

  public void addGold(int amount) { gold += Math.max(0, amount); }

  public void spendGold(int amount) {
    int a = Math.max(0, amount);
    if (gold < a) throw new IllegalStateException("Not enough gold");
    gold -= a;
  }

  /** Reduces gold by {@code percent} % (battle-loss penalty). */
  public void loseGoldPercent(int percent) {
    int p = Math.max(0, Math.min(100, percent));
    gold  = (int) Math.floor(gold * (1.0 - (p / 100.0)));
  }

  /** Strips {@code percent} % of each hero's in-level XP progress. */
  public void applyLossExpPenalty(int percent) {
    heroes.forEach(h -> h.loseExpProgressPercent(percent));
  }

  /** Fully heals and revives every hero (Inn effect). */
  public void fullRestore() { heroes.forEach(Hero::fullRestore); }

  public boolean isFull() { return heroes.size() >= 5; }

  public void addHero(Hero hero) {
    if (isFull()) return;
    heroes.add(Objects.requireNonNull(hero));
  }

}
