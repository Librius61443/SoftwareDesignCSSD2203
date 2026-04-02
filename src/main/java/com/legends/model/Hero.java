package com.legends.model;

public class Hero {
    
    private int level;
    private int attack;
    private int defense;
    private int healthPoints;
    private int manaPoints;

    public Hero(int level, int attack, int defense, int healthPoints, int manaPoints) {
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.healthPoints = healthPoints;
        this.manaPoints = manaPoints;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int getManaPoints() {
        return manaPoints;
    }

    public void setManaPoints(int manaPoints) {
        this.manaPoints = manaPoints;
    }
}