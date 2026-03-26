package unit;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * A player-controlled combatant.
 *
 * <p>Heroes use a single progression model for level-up growth.
 * XP required per level: {@code 500 + 75·L + 20·L²}.
 */
public final class Hero implements Unit {

  private final String name;

  private int level   = 1;
  private int attack  = 5;
  private int defense = 5;
  private int maxHp   = 100;
  private int hp      = 100;
  private int maxMana = 50;
  private int mana    = 50;

  /** XP accumulated toward the next level (no de-level on loss). */
  private int     expInLevel = 0;
  private boolean stunned    = false;

  public Hero(String name) {
    this.name = Objects.requireNonNull(name);
  }

  @Override public String name()    { return name; }
  @Override public int    level()   { return level; }
  @Override public int    attack()  { return attack; }
  @Override public int    defense() { return defense; }
  @Override public int    hp()      { return hp; }
  @Override public int    maxHp()   { return maxHp; }
  @Override public int    mana()    { return mana; }
  @Override public int    maxMana() { return maxMana; }
  @Override public Team   team()    { return Team.PLAYER; }

  @Override public void takeDamage(int dmg)    { hp   = Math.max(0, hp   - Math.max(0, dmg)); }
  @Override public void heal(int amount)        { hp   = Math.min(maxHp,   hp   + Math.max(0, amount)); }
  @Override public void restoreMana(int amount) { mana = Math.min(maxMana, mana + Math.max(0, amount)); }

  @Override public boolean stunned()   { return stunned; }
  @Override public void    clearStun() { stunned = false; }

  // --- Inn / full restore ---

  public void fullRestore() {
    hp      = maxHp;
    mana    = maxMana;
    stunned = false;
  }

  // --- Experience ---

  public void gainExp(int amount) {
    expInLevel += Math.max(0, amount);
    while (level < 20 && expInLevel >= expForNextLevel(level)) {
      expInLevel -= expForNextLevel(level);
      levelUp();
    }
  }

  /**
   * Removes {@code percent} % of in-level XP without causing a de-level.
   * Used for the battle-loss penalty.
   */
  public void loseExpProgressPercent(int percent) {
    int p  = clamp(percent, 0, 100);
    expInLevel = (int) Math.floor(expInLevel * (1.0 - (p / 100.0)));
  }

  // --- Private helpers ---

  private void levelUp() {
    level++;

    // Base increases every level
    attack  += 1;
    defense += 1;
    maxHp   += 5;
    maxMana += 2;

    hp   = Math.min(maxHp,   hp   + 5);
    mana = Math.min(maxMana, mana + 2);
  }

  /** XP required to advance from level {@code L} to {@code L+1}. */
  private int expForNextLevel(int L) {
    return 500 + (75 * L) + (20 * L * L);
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  // --- AI ---

  @Override
  public Action chooseAction(List<Unit> players, List<Unit> enemies, Queue<Unit> waitQueue) {
    if (hp > 0 && hp < (maxHp / 4)) return new DefendAction();
    return new AttackAction();
  }
}
