package com.legends.service;

public interface MatchState {
    void handleInvite(MatchContext context, boolean isAccepted);
    void selectParty(MatchContext context, String username, int partyId);
    void executeTurn(MatchContext context, String username, String action);
    void endMatch(MatchContext context);
}