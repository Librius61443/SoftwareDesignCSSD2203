package com.legends.service;

import com.legends.dao.DatabaseManager;
import com.legends.dao.UserDAO;
import com.legends.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingTest {

    private MatchmakingService matchmakingService;
    private UserDAO userDAO;
    private final String player1 = "testChallenger";
    private final String player2 = "testOpponent";

    private class TestObserver implements MatchmakingService.MatchObserver {
        boolean inviteReceived = false;
        boolean inviteAccepted = false;
        boolean inviteDeclined = false;

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
    }

    @BeforeEach
    public void setUp() {
        matchmakingService = new MatchmakingService();
        userDAO = new UserDAO();
        cleanUpDatabase();
        userDAO.createUser(new User(player1, "pass1"));
        userDAO.createUser(new User(player2, "pass2"));
    }

    @AfterEach
    public void tearDown() {
        cleanUpDatabase();
    }

    private void cleanUpDatabase() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username IN (?, ?)")) {
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public void testRespondToInviteAccepted() {
        TestObserver challengerObserver = new TestObserver();
        matchmakingService.registerObserver(player1, challengerObserver);

        matchmakingService.respondToInvite(player1, player2, true);

        assertTrue(challengerObserver.inviteAccepted);
        assertFalse(challengerObserver.inviteDeclined);
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