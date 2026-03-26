package com.legends.model;

import java.util.ArrayList;
import java.util.List;

public class Party {
    private int id;
    private String ownerUsername;
    private List<Hero> heroes;
    private int gold;

    public Party(int id, String ownerUsername) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.heroes = new ArrayList<>();
        this.gold = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public void setHeroes(List<Hero> heroes) {
        if (heroes != null && heroes.size() <= 5) {
            this.heroes = heroes;
        }
    }

    public void addHero(Hero hero) {
        if (this.heroes.size() < 5) {
            this.heroes.add(hero);
        }
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}