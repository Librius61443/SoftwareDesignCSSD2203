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
  private Team team;
  private HeroClass currentClass;
  private final ClassLevelTracker classTracker;

  private int level   = 1;
  private int attack  = 5;
  private int defense = 5;
  private int maxHp   = 100;
  private int hp      = 100;
  private int maxMana = 50;
  private int mana    = 50;
  private int shield  = 0;
  private boolean fireShield = false;

  /** XP accumulated toward the next level (no de-level on loss). */
  private int     expInLevel = 0;
  private boolean stunned    = false;

  public Hero(String name) {
    this(name, HeroClass.WARRIOR);
  }

  public Hero(String name, HeroClass startingClass) {
    this.name = Objects.requireNonNull(name);
    this.team = Team.PLAYER;
    this.currentClass = Objects.requireNonNull(startingClass);
    this.classTracker = new ClassLevelTracker();
    this.classTracker.investLevel(startingClass);
  }

  private Hero(String name, int level, int attack, int defense, int maxHp, int hp,
               int maxMana, int mana, int expInLevel, boolean stunned,
               HeroClass currentClass, ClassLevelTracker classTracker, int shield, Team team, boolean fireShield) {
    this.name = Objects.requireNonNull(name);
    this.level = level;
    this.attack = attack;
    this.defense = defense;
    this.maxHp = maxHp;
    this.hp = hp;
    this.maxMana = maxMana;
    this.mana = mana;
    this.expInLevel = expInLevel;
    this.stunned = stunned;
    this.currentClass = currentClass;
    this.classTracker = classTracker;
    this.shield = shield;
    this.team = team;
    this.fireShield = fireShield;
  }

  @Override public String name()    { return name; }
  @Override public int    level()   { return level; }
  @Override public int    attack()  { return attack; }
  @Override public int    defense() { return defense; }
  @Override public int    hp()      { return hp; }
  @Override public int    maxHp()   { return maxHp; }
  @Override public int    mana()    { return mana; }
  @Override public int    maxMana() { return maxMana; }
  @Override public Team   team()    { return team; }

  @Override public void takeDamage(int dmg)    {
    int applied = Math.max(0, dmg);
    int blocked = Math.min(shield, applied);
    shield -= blocked;
    hp = Math.max(0, hp - (applied - blocked));
  }
  @Override public void heal(int amount)        { hp   = Math.min(maxHp,   hp   + Math.max(0, amount)); }
  @Override public void restoreMana(int amount) { mana = Math.min(maxMana, mana + Math.max(0, amount)); }

  @Override public boolean stunned()   { return stunned; }
  @Override public void    clearStun() { stunned = false; }

  // --- Inn / full restore ---

  public void fullRestore() {
    hp      = maxHp;
    mana    = maxMana;
    shield  = 0;
    fireShield = false;
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
    ClassLevelTracker.LevelUpResult ignored = classTracker.investLevel(currentClass);
    level++;

    // Base increases every level
    attack  += 1;
    defense += 1;
    maxHp   += 5;
    maxMana += 2;

    attack  += classTracker.attackBonusForLevel(currentClass);
    defense += classTracker.defenseBonusForLevel(currentClass);
    maxHp   += classTracker.hpBonusForLevel(currentClass);
    maxMana += classTracker.manaBonusForLevel(currentClass);

    hp   = Math.min(maxHp,   hp   + 5);
    mana = Math.min(maxMana, mana + 2);
  }

  /** XP required to advance from level {@code L} to {@code L+1}.
   * Per spec: Exp(L) = Exp(L-1) + 500 + 75*L + 20*L^2
   * This method returns just the incremental cost for one level step. */
  private int expForNextLevel(int L) {
    return 500 + (75 * L) + (20 * L * L);
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  public Hero copy() {
    return new Hero(name, level, attack, defense, maxHp, hp, maxMana, mana, expInLevel, stunned,
        currentClass, classTracker.copy(), shield, team, fireShield);
  }

  public Hero copyForTeam(Team team) {
    Hero copy = copy();
    copy.team = Objects.requireNonNull(team);
    return copy;
  }

  public HeroClass currentClass() { return currentClass; }
  public void setCurrentClass(HeroClass currentClass) { this.currentClass = Objects.requireNonNull(currentClass); }
  public ClassLevelTracker classTracker() { return classTracker.copy(); }
  public HeroClass specializationClass() { return classTracker.specializationClass(); }
  public HybridClass hybridClass() { return classTracker.hybridClass(); }
  public int shield() { return shield; }
  public boolean isAlive() { return hp > 0; }
  public boolean hasFireShield() { return fireShield && shield > 0; }
  public void setFireShield(boolean fireShield) { this.fireShield = fireShield; }
  public boolean isSpecializedIn(HeroClass heroClass) { return hybridClass() == null && specializationClass() == heroClass; }
  public boolean isHybrid(HybridClass hybridClass) { return hybridClass() == hybridClass; }
  public boolean spendMana(int amount) {
    int cost = Math.max(0, amount);
    if (mana < cost) return false;
    mana -= cost;
    return true;
  }
  public void gainShield(int amount) { shield += Math.max(0, amount); }
  public void burnManaByPercent(int percent) {
    int manaLoss = Math.max(1, (maxMana * percent) / 100);
    mana = Math.max(0, mana - manaLoss);
  }
  public void setStunned(boolean stunned) { this.stunned = stunned; }

  // --- AI ---

  @Override
  public Action chooseAction(List<Unit> players, List<Unit> enemies, Queue<Unit> waitQueue) {
    Action castAction = chooseCastAction(players, enemies);
    if (castAction != null) return castAction;
    if (hp > 0 && hp < (maxHp / 4)) return new DefendAction();
    return new AttackAction();
  }

  private Action chooseCastAction(List<Unit> players, List<Unit> enemies) {
    return switch (currentClass) {
      case ORDER -> {
        boolean allyNeedsHealing = players.stream()
            .filter(Hero.class::isInstance)
            .map(Hero.class::cast)
            .anyMatch(hero -> hero.hp() < hero.maxHp());
        if (mana >= HeroAbility.HEAL.manaCost() && allyNeedsHealing) yield new CastAbilityAction(HeroAbility.HEAL);
        if (mana >= HeroAbility.PROTECT.manaCost()) yield new CastAbilityAction(HeroAbility.PROTECT);
        yield null;
      }
      case CHAOS -> {
        if (mana >= HeroAbility.CHAIN_LIGHTNING.manaCost() && enemies.stream().filter(Unit::isAlive).count() >= 2) {
          yield new CastAbilityAction(HeroAbility.CHAIN_LIGHTNING);
        }
        if (mana >= HeroAbility.FIREBALL.manaCost()) yield new CastAbilityAction(HeroAbility.FIREBALL);
        yield null;
      }
      case WARRIOR -> mana >= HeroAbility.BERSERKER_ATTACK.manaCost()
          ? new CastAbilityAction(HeroAbility.BERSERKER_ATTACK)
          : null;
      case MAGE -> {
        boolean allyNeedsMana = players.stream()
            .filter(Hero.class::isInstance)
            .map(Hero.class::cast)
            .anyMatch(hero -> hero.mana() < hero.maxMana());
        yield mana >= HeroAbility.REPLENISH.manaCost() && allyNeedsMana
            ? new CastAbilityAction(HeroAbility.REPLENISH)
            : null;
      }
    };
  }
}
