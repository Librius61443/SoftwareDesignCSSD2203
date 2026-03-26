package com.legends.controller;

import com.legends.model.User;
import com.legends.service.MatchmakingService;
import com.legends.service.MatchContext;
import com.legends.view.HubView;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PVPController implements MatchmakingService.MatchObserver {

    private HubView hubView;
    private MatchmakingService matchmakingService;
    private User currentUser;
    private MatchContext currentMatch;

    public PVPController(HubView hubView, User currentUser, MatchmakingService matchmakingService) {
        this.hubView = hubView;
        this.currentUser = currentUser;
        this.matchmakingService = matchmakingService;
        
        this.matchmakingService.registerObserver(currentUser.getUsername(), this);
        this.hubView.addSendInviteListener(new SendInviteListener());
    }

    class SendInviteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String targetUser = hubView.getInviteUsername();

            if (targetUser.isEmpty()) {
                hubView.displayErrorMessage("Please enter a username to invite.");
                return;
            }

            if (targetUser.equals(currentUser.getUsername())) {
                hubView.displayErrorMessage("You cannot invite yourself!");
                return;
            }

            boolean inviteSent = matchmakingService.sendInvite(currentUser.getUsername(), targetUser);
            
            if (inviteSent) {
                hubView.displaySuccessMessage("Invitation sent to " + targetUser + ". Waiting for response...");
            } else {
                hubView.displayErrorMessage("Could not send invite. User may be offline, nonexistent, or lack a saved party.");
            }
        }
    }

    @Override
    public void onInviteReceived(String senderUsername) {
        int response = hubView.showInvitePrompt(senderUsername);
        boolean accepted = (response == JOptionPane.YES_OPTION);

        matchmakingService.respondToInvite(senderUsername, currentUser.getUsername(), accepted);

        if (accepted) {
            currentMatch = new MatchContext(senderUsername, currentUser.getUsername());
            currentMatch.handleInvite(true);
            hubView.displaySuccessMessage("You accepted the match! Proceeding to Party Selection.");
        }
    }

    @Override
    public void onInviteAccepted(String receiverUsername) {
        hubView.displaySuccessMessage(receiverUsername + " accepted your invite! Proceeding to Party Selection.");
        currentMatch = new MatchContext(currentUser.getUsername(), receiverUsername);
        currentMatch.handleInvite(true);
    }

    @Override
    public void onInviteDeclined(String receiverUsername) {
        hubView.displayErrorMessage(receiverUsername + " declined your invitation.");
    }
}