package unit;

import java.util.List;
import java.util.Queue;


public interface Unit {

  String name();
  int    level();
  int    attack();
  int    defense();

  int  hp();
  int  maxHp();
  int  mana();
  int  maxMana();
  Team team();

  void takeDamage(int dmg);
  void heal(int amount);
  void restoreMana(int amount);

  boolean stunned();
  void    clearStun();

  Action chooseAction(List<Unit> players, List<Unit> enemies, Queue<Unit> waitQueue);
}
