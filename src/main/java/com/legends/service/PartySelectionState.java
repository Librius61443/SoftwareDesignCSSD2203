package com.legends.service;

public class PartySelectionState implements MatchState {

    @Override
    public void handleInvite(MatchContext context, boolean isAccepted) {
        throw new IllegalStateException("Invite has already been handled.");
    }

    @Override
    public void selectParty(MatchContext context, String username, int partyId) {
        // Assign the chosen party to the correct player
        if (username.equals(context.getChallenger())) {
            context.setChallengerPartyId(partyId);
            System.out.println(username + " locked in party " + partyId);
        } else if (username.equals(context.getOpponent())) {
            context.setOpponentPartyId(partyId);
            System.out.println(username + " locked in party " + partyId);
        }

        // Once both players have selected a party, transition to the actual battle
        if (context.getChallengerPartyId() != -1 && context.getOpponentPartyId() != -1) {
            context.setState(new ActiveCombatState());
            System.out.println("Both parties selected. Match is starting!");
        }
    }

    @Override
    public void executeTurn(MatchContext context, String username, String action) {
        throw new IllegalStateException("Cannot attack while still selecting parties.");
    }

    @Override
    public void endMatch(MatchContext context) {
        context.setState(new MatchEndedState());
    }
}