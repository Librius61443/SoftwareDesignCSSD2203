package unit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeroProgressionTest {

  @Test
  void orderHeroGetsClassStatGrowth() {
    Hero hero = new Hero("Cleric", HeroClass.ORDER);

    hero.gainExp(10_000);

    assertTrue(hero.level() > 1);
    assertTrue(hero.maxMana() > 52);
    assertTrue(hero.defense() > 6);
  }

  @Test
  void heroSpecializesAfterFiveLevelsInOneClass() {
    Hero hero = new Hero("Knight", HeroClass.WARRIOR);

    hero.gainExp(10_000);

    assertTrue(hero.level() >= 5);
    assertEquals(HeroClass.WARRIOR, hero.specializationClass());
  }

  @Test
  void heroHybridizesAfterReachingSecondClassThreshold() {
    Hero hero = new Hero("Hybrid", HeroClass.ORDER);
    hero.gainExp(10_000);
    hero.setCurrentClass(HeroClass.MAGE);
    hero.gainExp(20_000);

    assertEquals(HybridClass.PROPHET, hero.hybridClass());
  }
}
