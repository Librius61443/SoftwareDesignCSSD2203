package com.legends.service;

public class MatchEndedState implements MatchState {

    @Override
    public void handleInvite(MatchContext context, boolean isAccepted) {
        throw new IllegalStateException("The match has already ended.");
    }

    @Override
    public void selectParty(MatchContext context, String username, int partyId) {
        throw new IllegalStateException("The match has already ended.");
    }

    @Override
    public void executeTurn(MatchContext context, String username, String action) {
        throw new IllegalStateException("The match has already ended.");
    }

    @Override
    public void endMatch(MatchContext context) {
        System.out.println("Match is already over.");
    }
}