package com.legends.service;

import com.legends.dao.UserDAO;
import java.util.HashMap;
import java.util.Map;

public class MatchmakingService {

    // --------------------------------------------------------
    // OBSERVER PATTERN: Interface for the UI to implement
    // --------------------------------------------------------
    public interface MatchObserver {
        void onInviteReceived(String senderUsername);
        void onInviteAccepted(String receiverUsername);
        void onInviteDeclined(String receiverUsername);
        void onMatchCompleted(String summary);
    }

    // Maps online usernames to their specific active UI controllers
    private final Map<String, MatchObserver> activeClients;
    private final UserDAO userDAO;
    private final PartyService partyService;

    public MatchmakingService(PartyService partyService) {
        this.activeClients = new HashMap<>();
        this.userDAO = AppServices.userDAO();
        this.partyService = partyService;
    }

    // --------------------------------------------------------
    // OBSERVER PATTERN: Registration Methods
    // --------------------------------------------------------
    
    // Call this when a user logs in and enters the main HubView
    public void registerObserver(String username, MatchObserver observer) {
        activeClients.put(username, observer);
        System.out.println(username + " is now online and listening for invites.");
    }

    // Call this when a user logs out or leaves the hub
    public void removeObserver(String username) {
        activeClients.remove(username);
    }

    // --------------------------------------------------------
    // USE CASE 7: Matchmaking Logic
    // --------------------------------------------------------

    /**
     * Sends a PvP invitation to an exact username.
     */
    public boolean sendInvite(String senderUsername, String targetUsername) {
        // 1. Verify the exact username has a registered profile
        if (userDAO.getUser(targetUsername) == null) {
            System.err.println("Target user does not exist in the database.");
            return false;
        }

        // 2. Validate both users have saved parties (Conceptual placeholder for PartyDAO logic)
        // Both I and the invited player must have at least one saved party. [cite: 178]
        if (!partyService.hasSavedParties(senderUsername) || !partyService.hasSavedParties(targetUsername)) {
            System.err.println("Both players must have at least one saved party to battle.");
            return false;
        }

        // 3. OBSERVER PATTERN: Notify the target user if they are currently active/online
        MatchObserver targetObserver = activeClients.get(targetUsername);
        if (targetObserver != null) {
            targetObserver.onInviteReceived(senderUsername);
            return true;
        } else {
            System.err.println("User " + targetUsername + " is registered but not currently online.");
            return false;
        }
    }

    /**
     * Handles the target user's response to an invitation.
     */
    public void respondToInvite(String senderUsername, String receiverUsername, boolean accepted) {
        MatchObserver senderObserver = activeClients.get(senderUsername);
        
        if (accepted) {
            System.out.println(receiverUsername + " accepted the invite! Transitioning to Party Selection...");
            if (senderObserver != null) {
                senderObserver.onInviteAccepted(receiverUsername);
            }

            PvpBattleService.PvpBattleResult result =
                AppServices.pvpBattleService().battleFirstSavedParties(senderUsername, receiverUsername);
            String summary = result.winner() + " defeated " + result.loser() + " in PvP.";

            MatchObserver receiverObserver = activeClients.get(receiverUsername);
            if (senderObserver != null) {
                senderObserver.onMatchCompleted(summary);
            }
            if (receiverObserver != null) {
                receiverObserver.onMatchCompleted(summary);
            }
        } else {
            System.out.println(receiverUsername + " declined the invite.");
            if (senderObserver != null) {
                senderObserver.onInviteDeclined(receiverUsername);
            }
        }
    }
}
