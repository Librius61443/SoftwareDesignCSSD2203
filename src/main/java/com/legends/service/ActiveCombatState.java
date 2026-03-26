package com.legends.service;

public class ActiveCombatState implements MatchState {

    @Override
    public void handleInvite(MatchContext context, boolean isAccepted) {
        throw new IllegalStateException("Already in combat.");
    }

    @Override
    public void selectParty(MatchContext context, String username, int partyId) {
        throw new IllegalStateException("Parties have already been selected.");
    }

    @Override
    public void executeTurn(MatchContext context, String username, String action) {
        // This is where you will eventually link to your teammate's BattleSystem logic
        System.out.println(username + " performed action: " + action);
    }

    @Override
    public void endMatch(MatchContext context) {
        System.out.println("The battle has concluded!");
        context.setState(new MatchEndedState());
    }
}