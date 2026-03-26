package com.legends.controller;

import com.legends.battle.InteractiveBattleSession;
import com.legends.model.User;
import com.legends.service.AppServices;
import com.legends.service.CampaignRecordService;
import com.legends.service.CampaignService;
import com.legends.service.MatchmakingService;
import com.legends.service.MatchContext;
import com.legends.service.PartyService;
import com.legends.view.BattleView;
import com.legends.view.HubView;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import unit.HeroClass;
import unit.Party;

public class PVPController implements MatchmakingService.MatchObserver {

    private HubView hubView;
    private MatchmakingService matchmakingService;
    private User currentUser;
    private MatchContext currentMatch;
    private CampaignService campaignService;
    private PartyService partyService;
    private CampaignRecordService campaignRecordService;

    public PVPController(HubView hubView, User currentUser, MatchmakingService matchmakingService) {
        this.hubView = hubView;
        this.currentUser = currentUser;
        this.matchmakingService = matchmakingService;
        this.campaignService = AppServices.campaignService();
        this.partyService = AppServices.partyService();
        this.campaignRecordService = AppServices.campaignRecordService();
        
        this.matchmakingService.registerObserver(currentUser.getUsername(), this);
        this.hubView.addSendInviteListener(new SendInviteListener());
        this.hubView.addRunCampaignListener(new RunCampaignListener());
        this.hubView.addSavePartyListener(new SavePartyListener());
        this.hubView.addViewPartyListener(new ViewPartyListener());
        this.hubView.addViewHallOfFameListener(new ViewHallOfFameListener());
        this.hubView.setCampaignProgress(campaignService.getNextRoomNumber(currentUser.getUsername()), false);
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

    class RunCampaignListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            HeroClass selectedClass = hubView.getSelectedHeroClass();
            CampaignService.CampaignStepResult result = campaignService.playNextRoom(
                currentUser.getUsername(),
                currentUser.getUsername() + " Hero",
                selectedClass
            );
            if (result.pendingBattle() != null) {
                result = resolveBattle(result);
            }
            Party updatedParty = result.party();
            hubView.setCampaignProgress(campaignService.getNextRoomNumber(currentUser.getUsername()), result.campaignCompleted());
            if ("Inn".equals(result.encounterType())) {
                handleInnVisit(result);
            }
            if (result.campaignSummary() != null) {
                hubView.displaySuccessMessage("Campaign completed with score " + result.campaignSummary().score()
                    + ". Hall of Fame:\n" + formatHallOfFame(result.campaignSummary().hallOfFame()));
                return;
            }
            hubView.displaySuccessMessage("Room " + result.roomNumber() + " resolved as " + result.encounterType()
                + " (" + result.outcome() + "). Active party has " + updatedParty.heroes().size()
                + " hero(es), cumulative level " + updatedParty.cumulativeLevel() + ", gold "
                + updatedParty.gold() + ", class focus " + selectedClass.displayName() + ".");
        }
    }

    class SavePartyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean saved = campaignService.saveCurrentParty(currentUser.getUsername());
            if (saved) {
                int count = partyService.getSavedParties(currentUser.getUsername()).size();
                hubView.displaySuccessMessage("Active PvE party saved for PvP. Saved teams: " + count + "/"
                    + PartyService.MAX_SAVED_PARTIES + ".");
            } else {
                hubView.displayErrorMessage("Could not save party. Finish the 30-room campaign first or free up one of the 5 save slots.");
            }
        }
    }

    class ViewPartyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Party activeParty = partyService.getActiveParty(currentUser.getUsername());
            int savedCount = partyService.getSavedParties(currentUser.getUsername()).size();
            if (activeParty == null) {
                hubView.displayErrorMessage("No active party found yet.");
                return;
            }

            hubView.displaySuccessMessage("Active party: " + activeParty.heroes().size() + " hero(es), cumulative level "
                + activeParty.cumulativeLevel() + ", gold " + activeParty.gold() + ". Saved PvP parties: " + savedCount
                + ". Next room: " + campaignService.getNextRoomNumber(currentUser.getUsername()) + ".");
        }
    }

    class ViewHallOfFameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            hubView.displaySuccessMessage("Hall of Fame:\n" + formatHallOfFame(campaignRecordService.getHallOfFame()));
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

    @Override
    public void onMatchCompleted(String summary) {
        hubView.displaySuccessMessage(summary);
    }

    private void handleInnVisit(CampaignService.CampaignStepResult result) {
        List<CampaignService.InnItemOption> items = result.innItems();
        List<String> heroNames = result.party().heroes().stream().map(unit.Hero::name).toList();
        while (!items.isEmpty()) {
            String choice = hubView.promptInnPurchase(items.stream()
                .map(item -> item.name() + " (" + item.cost() + "g, " + item.effect() + ")")
                .toList());
            if (choice == null) {
                break;
            }
            String itemName = choice.substring(0, choice.indexOf(" ("));
            String heroTarget = hubView.promptHeroTarget(heroNames, itemName);
            if (heroTarget == null) {
                break;
            }
            if (!campaignService.buyInnItem(currentUser.getUsername(), itemName, heroTarget)) {
                hubView.displayErrorMessage("Could not buy " + itemName + ".");
                break;
            }
        }

        CampaignService.RecruitOption recruit = campaignService.getPendingRecruit(currentUser.getUsername());
        if (recruit != null) {
            boolean recruitAccepted = hubView.confirmRecruit(
                recruit.name() + " - " + recruit.heroClass().displayName() + " level " + recruit.level()
                    + " for " + recruit.cost() + " gold"
            );
            if (recruitAccepted && !campaignService.recruitFromInn(currentUser.getUsername())) {
                hubView.displayErrorMessage("Could not recruit hero.");
            }
        }
        campaignService.endInnVisit(currentUser.getUsername());
    }

    private String formatHallOfFame(List<CampaignRecordService.HallOfFameEntry> entries) {
        if (entries.isEmpty()) {
            return "No completed campaigns yet.";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            CampaignRecordService.HallOfFameEntry entry = entries.get(i);
            builder.append(i + 1).append(". ").append(entry.username()).append(" - ").append(entry.score());
            if (i < entries.size() - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private CampaignService.CampaignStepResult resolveBattle(CampaignService.CampaignStepResult result) {
        BattleView battleView = new BattleView(hubView, "Battle - Room " + result.roomNumber());
        InteractiveBattleSession session = new InteractiveBattleSession(
            result.pendingBattle().party(),
            result.pendingBattle().enemies()
        );
        BattleController controller = new BattleController(session, battleView);
        InteractiveBattleSession.BattleOutcome outcome = controller.runBattle();
        return campaignService.resolvePendingBattle(
            currentUser.getUsername(),
            outcome.winner() == unit.Team.PLAYER,
            result.pendingBattle().party()
        );
    }
}
