package unit;

import java.util.List;
import java.util.Queue;

/**
 * An enemy combatant.
 *
 * <p>Enemies have no special abilities; they ATTACK unless critically low on
 * HP, in which case they DEFEND.
 */
public final class EnemyUnit implements Unit {

  private final String  name;
  private final int     level;
  private final int     attack;
  private final int     defense;
  private final int     maxHp;
  private       int     hp;
  private final int     maxMana;
  private       int     mana;
  private       boolean stunned = false;

  public EnemyUnit(String name, int level, int attack, int defense, int maxHp, int maxMana) {
    this.name    = name;
    this.level   = level;
    this.attack  = attack;
    this.defense = defense;
    this.maxHp   = maxHp;
    this.hp      = maxHp;
    this.maxMana = maxMana;
    this.mana    = maxMana;
  }

  @Override public String  name()    { return name; }
  @Override public int     level()   { return level; }
  @Override public int     attack()  { return attack; }
  @Override public int     defense() { return defense; }
  @Override public int     hp()      { return hp; }
  @Override public int     maxHp()   { return maxHp; }
  @Override public int     mana()    { return mana; }
  @Override public int     maxMana() { return maxMana; }
  @Override public Team    team()    { return Team.ENEMY; }

  @Override public void takeDamage(int dmg)    { hp   = Math.max(0, hp   - Math.max(0, dmg)); }
  @Override public void heal(int amount)        { hp   = Math.min(maxHp,   hp   + Math.max(0, amount)); }
  @Override public void restoreMana(int amount) { mana = Math.min(maxMana, mana + Math.max(0, amount)); }

  @Override public boolean stunned()   { return stunned; }
  @Override public void    clearStun() { stunned = false; }

  @Override
  public Action chooseAction(List<Unit> players, List<Unit> enemies, Queue<Unit> waitQueue) {
    if (hp <= 0)           return new WaitAction();
    if (hp < (maxHp / 5)) return new DefendAction();
    return new AttackAction();
  }
}
