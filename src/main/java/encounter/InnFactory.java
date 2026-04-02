package encounter;

import rng.Rng;

import java.util.Objects;

/** Assembles an {@link InnEncounter} with a shop and recruit service. */
public final class InnFactory {

  private final Rng rng;

  public InnFactory(Rng rng) {
    this.rng = Objects.requireNonNull(rng);
  }

  public Encounter createInn() {
    return new InnEncounter(rng, new SimpleInnShop(), new RecruitService(rng));
  }
}
