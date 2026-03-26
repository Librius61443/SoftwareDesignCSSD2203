package com.legends.model.item;

import com.legends.model.hero.Hero;

public interface Item {
    String getName();
    int getCost();
    String getDescription();
    void applyTo(Hero hero);
}
