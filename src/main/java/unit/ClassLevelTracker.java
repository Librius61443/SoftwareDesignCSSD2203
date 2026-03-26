package unit;

import java.util.EnumMap;
import java.util.Map;

public final class ClassLevelTracker {

  private static final int SPECIALIZATION_THRESHOLD = 5;

  private final Map<HeroClass, Integer> classLevels = new EnumMap<>(HeroClass.class);
  private HybridClass hybridClass;
  private HeroClass specializationClass;

  public ClassLevelTracker() {
    for (HeroClass heroClass : HeroClass.values()) {
      classLevels.put(heroClass, 0);
    }
  }

  private ClassLevelTracker(Map<HeroClass, Integer> classLevels, HybridClass hybridClass,
                            HeroClass specializationClass) {
    this();
    this.classLevels.putAll(classLevels);
    this.hybridClass = hybridClass;
    this.specializationClass = specializationClass;
  }

  public LevelUpResult investLevel(HeroClass chosenClass) {
    classLevels.merge(chosenClass, 1, Integer::sum);
    boolean justSpecialized = false;
    boolean justHybridized = false;

    if (hybridClass == null && classLevels.get(chosenClass) >= SPECIALIZATION_THRESHOLD) {
      if (specializationClass == null) {
        specializationClass = chosenClass;
        justSpecialized = true;
      } else if (specializationClass != chosenClass) {
        hybridClass = HybridClass.of(specializationClass, chosenClass);
        specializationClass = null;
        justHybridized = true;
      }
    }

    return new LevelUpResult(chosenClass, specializationClass, hybridClass, justSpecialized, justHybridized);
  }

  public int attackBonusForLevel(HeroClass chosenClass) {
    int base = hybridClass != null ? hybridClass.attackBonusPerLevel() : chosenClass.attackBonusPerLevel();
    return base * growthMultiplier(chosenClass);
  }

  public int defenseBonusForLevel(HeroClass chosenClass) {
    int base = hybridClass != null ? hybridClass.defenseBonusPerLevel() : chosenClass.defenseBonusPerLevel();
    return base * growthMultiplier(chosenClass);
  }

  public int hpBonusForLevel(HeroClass chosenClass) {
    int base = hybridClass != null ? hybridClass.hpBonusPerLevel() : chosenClass.hpBonusPerLevel();
    return base * growthMultiplier(chosenClass);
  }

  public int manaBonusForLevel(HeroClass chosenClass) {
    int base = hybridClass != null ? hybridClass.manaBonusPerLevel() : chosenClass.manaBonusPerLevel();
    return base * growthMultiplier(chosenClass);
  }

  public int levelInClass(HeroClass heroClass) {
    return classLevels.getOrDefault(heroClass, 0);
  }

  public HybridClass hybridClass() {
    return hybridClass;
  }

  public HeroClass specializationClass() {
    return specializationClass;
  }

  public boolean isHybridized() {
    return hybridClass != null;
  }

  public ClassLevelTracker copy() {
    return new ClassLevelTracker(new EnumMap<>(classLevels), hybridClass, specializationClass);
  }

  private int growthMultiplier(HeroClass chosenClass) {
    return hybridClass == null && specializationClass != null && specializationClass == chosenClass ? 2 : 1;
  }

  public record LevelUpResult(HeroClass classChosen, HeroClass newSpecialization,
                              HybridClass newHybrid, boolean justSpecialized,
                              boolean justHybridized) {}
}
