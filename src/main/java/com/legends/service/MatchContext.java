package com.legends.service;

public class MatchContext {
    private MatchState currentState;
    private String challenger;
    private String opponent;
    private int challengerPartyId;
    private int opponentPartyId;

    public MatchContext(String challenger, String opponent) {
        this.challenger = challenger;
        this.opponent = opponent;
        this.currentState = new InvitePendingState();
        this.challengerPartyId = -1;
        this.opponentPartyId = -1;
    }

    public void setState(MatchState state) {
        this.currentState = state;
    }

    public MatchState getState() {
        return currentState;
    }

    public void handleInvite(boolean isAccepted) {
        currentState.handleInvite(this, isAccepted);
    }

    public void selectParty(String username, int partyId) {
        currentState.selectParty(this, username, partyId);
    }

    public void executeTurn(String username, String action) {
        currentState.executeTurn(this, username, action);
    }

    public void endMatch() {
        currentState.endMatch(this);
    }

    public String getChallenger() {
        return challenger;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setChallengerPartyId(int partyId) {
        this.challengerPartyId = partyId;
    }

    public int getChallengerPartyId() {
        return challengerPartyId;
    }

    public void setOpponentPartyId(int partyId) {
        this.opponentPartyId = partyId;
    }

    public int getOpponentPartyId() {
        return opponentPartyId;
    }
}