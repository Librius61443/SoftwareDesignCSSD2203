package rng;

/** Abstraction over random-number generation. */
public interface Rng {
  int nextInt(int minInclusive, int maxInclusive);
}
