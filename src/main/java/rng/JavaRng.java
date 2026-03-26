package rng;

import java.util.Objects;
import java.util.Random;

/** Standard Java {@link Random}-backed RNG. */
public final class JavaRng implements Rng {

  private final Random random;

  public JavaRng(Random random) {
    this.random = Objects.requireNonNull(random);
  }

  @Override
  public int nextInt(int minInclusive, int maxInclusive) {
    if (maxInclusive < minInclusive) return minInclusive;
    int bound = (maxInclusive - minInclusive) + 1;
    return minInclusive + random.nextInt(bound);
  }
}
