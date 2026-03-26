package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;

import java.util.*;

/**
 * Computes and manages turn order for PvP battles.
 *
 * Rules:
 * - Units act in descending level order. Ties broken by attack.
 * - Teams alternate: once the highest-level unit is determined, teams alternate turns.
 * - Waiting units are moved to a FIFO queue at the end of the round.
 * - Stunned units lose their turn.
 */
public class TurnOrder {

    private List<Hero> turnQueue;
    private Queue<Hero> waitingQueue;
    private Party party1;
    private Party party2;

    public TurnOrder(Party party1, Party party2) {
        this.party1 = party1;
        this.party2 = party2;
        this.waitingQueue = new LinkedList<>();
        this.turnQueue = new ArrayList<>();
        buildTurnOrder();
    }

    /**
     * Build the interleaved turn order for a round.
     * Highest level (or attack on tie) unit goes first, then teams alternate.
     */
    private void buildTurnOrder() {
        turnQueue.clear();

        List<Hero> p1Alive = new ArrayList<>(party1.getAliveHeroes());
        List<Hero> p2Alive = new ArrayList<>(party2.getAliveHeroes());

        // Sort each party by level desc, then attack desc
        Comparator<Hero> comp = Comparator.comparingInt(Hero::getLevel)
                .thenComparingInt(Hero::getAttack).reversed();
        p1Alive.sort(comp);
        p2Alive.sort(comp);

        // Determine which party goes first (highest individual level/attack)
        boolean p1First = true;
        if (!p1Alive.isEmpty() && !p2Alive.isEmpty()) {
            Hero h1 = p1Alive.get(0);
            Hero h2 = p2Alive.get(0);
            if (h2.getLevel() > h1.getLevel() ||
                (h2.getLevel() == h1.getLevel() && h2.getAttack() > h1.getAttack())) {
                p1First = false;
            }
        }

        // Interleave: first party takes first slot, then second, alternating
        int i = 0, j = 0;
        boolean p1Turn = p1First;
        while (i < p1Alive.size() || j < p2Alive.size()) {
            if (p1Turn && i < p1Alive.size()) {
                turnQueue.add(p1Alive.get(i++));
                p1Turn = false;
            } else if (!p1Turn && j < p2Alive.size()) {
                turnQueue.add(p2Alive.get(j++));
                p1Turn = true;
            } else if (i < p1Alive.size()) {
                turnQueue.add(p1Alive.get(i++));
            } else {
                turnQueue.add(p2Alive.get(j++));
            }
        }

        // Append waiting queue at the end
        turnQueue.addAll(waitingQueue);
        waitingQueue.clear();
    }

    /**
     * Rebuild the turn order for a new round (called after all heroes have acted).
     */
    public void rebuildForNewRound() {
        buildTurnOrder();
    }

    public List<Hero> getTurnQueue() {
        return Collections.unmodifiableList(turnQueue);
    }

    /** Mark a hero as waiting — remove from current position and add to FIFO queue. */
    public void heroWaits(Hero hero) {
        turnQueue.remove(hero);
        waitingQueue.add(hero);
    }

    /** Returns which party a hero belongs to. */
    public Party getPartyOf(Hero hero) {
        if (party1.getHeroes().contains(hero)) return party1;
        if (party2.getHeroes().contains(hero)) return party2;
        return null;
    }

    /** Returns the opposing party of the given party. */
    public Party getOpposingParty(Party party) {
        return party == party1 ? party2 : party1;
    }

    public Party getParty1() { return party1; }
    public Party getParty2() { return party2; }
}
