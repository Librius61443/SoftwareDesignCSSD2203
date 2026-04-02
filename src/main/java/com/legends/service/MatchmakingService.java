package com.legends.service;

import com.legends.dao.UserDAO;
import java.util.HashMap;
import java.util.Map;

public class MatchmakingService {

    public interface MatchObserver {
        void onInviteReceived(String senderUsername);
        void onInviteAccepted(String receiverUsername);
        void onInviteDeclined(String receiverUsername);
        void onMatchCompleted(String summary);
    }

    private final Map<String, MatchObserver> activeClients = new HashMap<>();
    private final Map<String, MatchContext> activeMatches = new HashMap<>();
    private final UserDAO userDAO;
    private final PartyService partyService;

    public MatchmakingService(PartyService partyService) {
        this.userDAO = AppServices.userDAO();
        this.partyService = partyService;
    }

    public void registerObserver(String username, MatchObserver observer) {
        activeClients.put(username, observer);
        System.out.println(username + " is now online and listening for invites.");
    }

    public void removeObserver(String username) {
        activeClients.remove(username);
    }

    public boolean sendInvite(String senderUsername, String targetUsername) {
        if (userDAO.getUser(targetUsername) == null) {
            System.err.println("Target user does not exist.");
            return false;
        }
        if (!partyService.hasSavedParties(senderUsername) || !partyService.hasSavedParties(targetUsername)) {
            System.err.println("Both players must have at least one saved party to battle.");
            return false;
        }
        MatchObserver targetObserver = activeClients.get(targetUsername);
        if (targetObserver != null) {
            targetObserver.onInviteReceived(senderUsername);
            return true;
        } else {
            System.err.println("User " + targetUsername + " is not currently online.");
            return false;
        }
    }

    public void respondToInvite(String senderUsername, String receiverUsername, boolean accepted) {
        MatchObserver senderObserver = activeClients.get(senderUsername);
        if (accepted) {
            System.out.println(receiverUsername + " accepted the invite!");
            if (senderObserver != null) {
                senderObserver.onInviteAccepted(receiverUsername);
            }
        } else {
            System.out.println(receiverUsername + " declined the invite.");
            if (senderObserver != null) {
                senderObserver.onInviteDeclined(receiverUsername);
            }
        }
    }

    /**
     * Returns a shared MatchContext for this pair of players, creating it if
     * it doesn't exist yet. Both controllers call this so they operate on the
     * exact same object — meaning selectParty() calls from either side update
     * the same challenger/opponent party ID fields.
     */
    public MatchContext getOrCreateMatch(String challenger, String opponent) {
        String key = matchKey(challenger, opponent);
        return activeMatches.computeIfAbsent(key, k -> new MatchContext(challenger, opponent));
    }

    /** Remove the shared MatchContext once the battle is over. */
    public void clearMatch(String challenger, String opponent) {
        activeMatches.remove(matchKey(challenger, opponent));
    }

    public void notifyMatchCompleted(String player1, String player2, String summary) {
        MatchObserver obs1 = activeClients.get(player1);
        MatchObserver obs2 = activeClients.get(player2);
        if (obs1 != null) obs1.onMatchCompleted(summary);
        if (obs2 != null) obs2.onMatchCompleted(summary);
    }

    /** Canonical key — always challenger first so both sides produce the same key. */
    private String matchKey(String challenger, String opponent) {
        return challenger + ":" + opponent;
    }
}
