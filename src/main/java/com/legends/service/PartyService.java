package com.legends.service;

import unit.Party;
import unit.HeroClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the campaign party currently being worked on by each user and up to
 * five PvP-ready saved parties produced by PvE runs.
 */
public class PartyService {

    public static final int MAX_SAVED_PARTIES = 5;

    private final Map<String, Party> activeParties = new ConcurrentHashMap<>();
    private final Map<String, List<Party>> savedParties = new ConcurrentHashMap<>();

    public Party getOrCreateActiveParty(String username, String starterHeroName) {
        return activeParties.computeIfAbsent(username, ignored -> Party.singleStarterHero(starterHeroName));
    }

    public Party getOrCreateActiveParty(String username, String starterHeroName, HeroClass starterClass) {
        return activeParties.computeIfAbsent(username, ignored -> Party.singleStarterHero(starterHeroName, starterClass));
    }

    public Party getActiveParty(String username) {
        Party party = activeParties.get(username);
        return party == null ? null : party.copy();
    }

    public void setActiveParty(String username, Party party) {
        activeParties.put(username, party.copy());
    }

    public boolean hasSavedParties(String username) {
        return !savedParties.getOrDefault(username, Collections.emptyList()).isEmpty();
    }

    public List<Party> getSavedParties(String username) {
        List<Party> parties = savedParties.getOrDefault(username, Collections.emptyList());
        List<Party> copies = new ArrayList<>(parties.size());
        for (Party party : parties) {
            copies.add(party.copy());
        }
        return copies;
    }

    public Party getSavedParty(String username, int index) {
        List<Party> parties = savedParties.getOrDefault(username, Collections.emptyList());
        if (index < 0 || index >= parties.size()) {
            return null;
        }
        return parties.get(index).copy();
    }

    public boolean saveActiveParty(String username) {
        Party activeParty = activeParties.get(username);
        if (activeParty == null) {
            return false;
        }

        List<Party> parties = savedParties.computeIfAbsent(username, ignored -> new ArrayList<>());
        if (parties.size() >= MAX_SAVED_PARTIES) {
            return false;
        }

        parties.add(activeParty.copy());
        return true;
    }

    public void reset() {
        activeParties.clear();
        savedParties.clear();
    }
}
