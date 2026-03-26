package com.legendsofswordandwand.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a party of heroes (1 to 5 members).
 */
public class Party {

    private static final int MAX_SIZE = 5;

    private String ownerName;
    private List<Hero> heroes;

    public Party(String ownerName) {
        this.ownerName = ownerName;
        this.heroes = new ArrayList<>();
    }

    /** Add a hero to the party. Max 5 heroes. */
    public boolean addHero(Hero hero) {
        if (heroes.size() >= MAX_SIZE) return false;
        heroes.add(hero);
        return true;
    }

    public void removeHero(Hero hero) {
        heroes.remove(hero);
    }

    /** Returns only heroes that are alive (HP > 0). */
    public List<Hero> getAliveHeroes() {
        return heroes.stream().filter(Hero::isAlive).collect(Collectors.toList());
    }

    /** Returns true if all heroes are dead. */
    public boolean isDefeated() {
        return heroes.stream().noneMatch(Hero::isAlive);
    }

    /** Cumulative level of all heroes. */
    public int getCumulativeLevel() {
        return heroes.stream().mapToInt(Hero::getLevel).sum();
    }

    /** Hero with the lowest current HP among alive heroes. */
    public Hero getLowestHpHero() {
        return getAliveHeroes().stream()
                .min((a, b) -> Integer.compare(a.getCurrentHp(), b.getCurrentHp()))
                .orElse(null);
    }

    public String getOwnerName() { return ownerName; }
    public List<Hero> getHeroes() { return heroes; }
    public int getSize() { return heroes.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Party[" + ownerName + "]:\n");
        for (Hero h : heroes) sb.append("  ").append(h).append("\n");
        return sb.toString();
    }
}
