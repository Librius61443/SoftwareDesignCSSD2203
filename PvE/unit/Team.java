package unit;

/** Which side of a battle a unit belongs to. */
public enum Team {
  PLAYER, ENEMY;

  public Team other() { return this == PLAYER ? ENEMY : PLAYER; }
}
