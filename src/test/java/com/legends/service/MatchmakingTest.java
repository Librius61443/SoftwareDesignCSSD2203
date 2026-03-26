package com.legends.service;

import com.legends.dao.UserDAO;
import com.legends.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unit.Hero;
import unit.Party;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingTest {

    private MatchmakingService matchmakingService;
    private PartyService partyService;
    private UserDAO userDAO;
    private final String player1 = "testChallenger";
    private final String player2 = "testOpponent";

    private static class TestObserver implements MatchmakingService.MatchObserver {
        boolean inviteReceived = false;
        boolean inviteAccepted = false;
        boolean inviteDeclined = false;
        String matchSummary;

        @Override
        public void onInviteReceived(String senderUsername) {
            inviteReceived = true;
        }

        @Override
        public void onInviteAccepted(String receiverUsername) {
            inviteAccepted = true;
        }

        @Override
        public void onInviteDeclined(String receiverUsername) {
            inviteDeclined = true;
        }

        @Override
        public void onMatchCompleted(String summary) {
            matchSummary = summary;
        }
    }

    @BeforeEach
    public void setUp() {
        userDAO = AppServices.userDAO();
        userDAO.resetInMemoryStore();
        partyService = AppServices.partyService();
        partyService.reset();
        matchmakingService = new MatchmakingService(partyService);

        userDAO.createUser(new User(player1, "pass1"));
        userDAO.createUser(new User(player2, "pass2"));

        Party challengerParty = Party.singleStarterHero("Challenger");
        challengerParty.addHero(new Hero("Backup"));
        partyService.setActiveParty(player1, challengerParty);
        partyService.saveActiveParty(player1);

        Party opponentParty = Party.singleStarterHero("Opponent");
        partyService.setActiveParty(player2, opponentParty);
        partyService.saveActiveParty(player2);
    }

    @Test
    public void testSendInviteSuccess() {
        TestObserver opponentObserver = new TestObserver();
        matchmakingService.registerObserver(player2, opponentObserver);

        boolean inviteSent = matchmakingService.sendInvite(player1, player2);

        assertTrue(inviteSent);
        assertTrue(opponentObserver.inviteReceived);
    }

    @Test
    public void testSendInviteToOfflineUser() {
        boolean inviteSent = matchmakingService.sendInvite(player1, player2);

        assertFalse(inviteSent);
    }

    @Test
    public void testSendInviteToNonexistentUser() {
        boolean inviteSent = matchmakingService.sendInvite(player1, "ghostUser");

        assertFalse(inviteSent);
    }

    @Test
    public void testRespondToInviteAcceptedCompletesMatch() {
        TestObserver challengerObserver = new TestObserver();
        TestObserver opponentObserver = new TestObserver();
        matchmakingService.registerObserver(player1, challengerObserver);
        matchmakingService.registerObserver(player2, opponentObserver);

        matchmakingService.respondToInvite(player1, player2, true);

        assertTrue(challengerObserver.inviteAccepted);
        assertNotNull(challengerObserver.matchSummary);
        assertNotNull(opponentObserver.matchSummary);
    }

    @Test
    public void testRespondToInviteDeclined() {
        TestObserver challengerObserver = new TestObserver();
        matchmakingService.registerObserver(player1, challengerObserver);

        matchmakingService.respondToInvite(player1, player2, false);

        assertTrue(challengerObserver.inviteDeclined);
        assertFalse(challengerObserver.inviteAccepted);
    }
}
