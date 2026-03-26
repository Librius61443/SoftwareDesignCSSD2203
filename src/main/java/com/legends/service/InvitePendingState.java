package com.legends.service;

public class InvitePendingState implements MatchState {

    @Override
    public void handleInvite(MatchContext context, boolean isAccepted) {
        if (isAccepted) {
            context.setState(new PartySelectionState());
        } else {
            context.setState(new MatchEndedState());
        }
    }

    @Override
    public void selectParty(MatchContext context, String username, int partyId) {
        throw new IllegalStateException();
    }

    @Override
    public void executeTurn(MatchContext context, String username, String action) {
        throw new IllegalStateException();
    }

    @Override
    public void endMatch(MatchContext context) {
        context.setState(new MatchEndedState());
    }
}
