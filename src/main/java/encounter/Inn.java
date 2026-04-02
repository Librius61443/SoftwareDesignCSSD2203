package encounter;

import rng.Rng;
import unit.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


interface Shop {
  List<Item> itemsForSale();
  void buy(Party party, Item item);
}

interface Item {
  String name();
  int    costGold();
  void   apply(Party party);
}


/**
 * Resolves an inn room: full restore, optional auto-buy, optional recruit
 * (rooms 1–10).
 */
final class InnEncounter implements Encounter {

  private final Rng            rng;
  private final Shop           shop;
  private final RecruitService recruitService;

  public InnEncounter(Rng rng, Shop shop, RecruitService recruitService) {
    this.rng            = Objects.requireNonNull(rng);
    this.shop           = Objects.requireNonNull(shop);
    this.recruitService = Objects.requireNonNull(recruitService);
  }

  @Override
  public EncounterOutcome resolve(RoomContext ctx, Party party) {
    party.fullRestore();
    return EncounterOutcome.NEUTRAL;
  }
}


final class SimpleInnShop implements Shop {

  private final List<Item> catalogue = List.of(
      new FoodItem("Bread",  200,   20),
      new FoodItem("Cheese", 500,   50),
      new FoodItem("Steak",  1000, 200),
      new DrinkItem("Water", 150,   10),
      new DrinkItem("Juice", 400,   30),
      new DrinkItem("Wine",  750,  100),
      new ElixirItem("Elixir", 2000)
  );

  @Override public List<Item> itemsForSale() { return catalogue; }

  @Override
  public void buy(Party party, Item item) {
    party.spendGold(item.costGold());
    item.apply(party);
  }
}


/** Restores HP to the hero with the lowest current HP. */
final class FoodItem implements Item {
  private final String name;
  private final int    cost;
  private final int    hpRestore;

  FoodItem(String name, int costGold, int hpRestore) {
    this.name = name; this.cost = costGold; this.hpRestore = hpRestore;
  }

  @Override public String name()     { return name; }
  @Override public int    costGold() { return cost; }

  @Override
  public void apply(Party party) {
    party.heroes().stream()
        .filter(h -> h.hp() > 0)
        .min(Comparator.comparingInt(Hero::hp))
        .ifPresent(h -> h.heal(hpRestore));
  }
}

/** Restores mana to the hero with the lowest current mana. */
final class DrinkItem implements Item {
  private final String name;
  private final int    cost;
  private final int    manaRestore;

  DrinkItem(String name, int costGold, int manaRestore) {
    this.name = name; this.cost = costGold; this.manaRestore = manaRestore;
  }

  @Override public String name()     { return name; }
  @Override public int    costGold() { return cost; }

  @Override
  public void apply(Party party) {
    party.heroes().stream()
        .filter(h -> h.hp() > 0)
        .min(Comparator.comparingInt(Hero::mana))
        .ifPresent(h -> h.restoreMana(manaRestore));
  }
}

/** Fully revives and restores the first dead hero. */
final class ElixirItem implements Item {
  private final String name;
  private final int    cost;

  ElixirItem(String name, int costGold) {
    this.name = name; this.cost = costGold;
  }

  @Override public String name()     { return name; }
  @Override public int    costGold() { return cost; }

  @Override
  public void apply(Party party) {
    party.heroes().stream()
        .filter(h -> h.hp() == 0)
        .findFirst()
        .ifPresent(Hero::fullRestore);
  }
}

/**
 * Attempts to add a new hero to the party when visiting an inn in rooms 1–10.
 *
 * <p>Level-1 recruits are free; higher-level recruits cost 200 × level gold.
 */
final class RecruitService {

  private final Rng rng;

  RecruitService(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public void maybeRecruit(Party party, int roomIndex) {
    if (party.isFull()) return;
    if (rng.nextInt(1, 100) > 35) return;   // 35 % chance

    int         level     = rng.nextInt(1, 4);
    int         cost      = (level == 1) ? 0 : (200 * level);

    if (party.gold() < cost) return;
    if (cost > 0) party.spendGold(cost);

    HeroClass[] classes = HeroClass.values();
    Hero recruit = new Hero(
        "Recruit-" + roomIndex + "-" + rng.nextInt(100, 999),
        classes[rng.nextInt(0, classes.length - 1)]
    );
    while (recruit.level() < level) recruit.gainExp(999_999);
    party.addHero(recruit);
  }
}
