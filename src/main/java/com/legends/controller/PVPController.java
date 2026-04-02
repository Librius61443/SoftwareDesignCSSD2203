package com.legends.controller;

import com.legends.battle.InteractiveBattleSession;
import com.legends.model.User;
import com.legends.service.AppServices;
import com.legends.service.CampaignRecordService;
import com.legends.service.CampaignService;
import com.legends.service.MatchmakingService;
import com.legends.service.MatchContext;
import com.legends.service.PartyService;
import com.legends.service.PvpBattleService;
import com.legends.controller.PvpBattleController;
import com.legends.view.BattleView;
import com.legends.view.HubView;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
    private com.legends.view.HomeView homeView;

    public PVPController(HubView hubView, User currentUser, MatchmakingService matchmakingService) {
        this(hubView, currentUser, matchmakingService, null);
    }

    public PVPController(HubView hubView, User currentUser, MatchmakingService matchmakingService,
                         com.legends.view.HomeView homeView) {
        this.hubView = hubView;
        this.currentUser = currentUser;
        this.matchmakingService = matchmakingService;
        this.homeView = homeView;
        this.campaignService = AppServices.campaignService();
        this.partyService = AppServices.partyService();
        this.campaignRecordService = AppServices.campaignRecordService();

        this.matchmakingService.registerObserver(currentUser.getUsername(), this);
        this.hubView.addSendInviteListener(new SendInviteListener());
        this.hubView.addRunCampaignListener(new RunCampaignListener());
        this.hubView.addSavePartyListener(new SavePartyListener());
        this.hubView.addViewPartyListener(new ViewPartyListener());
        this.hubView.addViewHallOfFameListener(new ViewHallOfFameListener());
        this.hubView.addExitCampaignListener(new ExitCampaignListener());
        this.hubView.addLogoutListener(new LogoutListener());
        this.hubView.addBackListener(new BackListener());

        int nextRoom = campaignService.getNextRoomNumber(currentUser.getUsername());
        this.hubView.setCampaignProgress(nextRoom,
            campaignService.isCampaignCompleted(currentUser.getUsername()));
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
                hubView.appendStatus("Invitation sent to " + targetUser + ". Waiting for response...");
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
                hubView.displayErrorMessage("Could not save party. You may not have an active party yet, or all 5 save slots are full.");
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

    /**
     * Saves the current campaign progress and stays on the hub (Use Case 5).
     * Blocked while in an active battle.
     */
    class ExitCampaignListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                boolean saved = campaignService.saveCampaignProgress(currentUser.getUsername());
                if (saved) {
                    int savedRoom = campaignService.getNextRoomNumber(currentUser.getUsername());
                    hubView.displaySuccessMessage(
                        "Campaign progress saved at room " + savedRoom + ".\n"
                        + "Your party and inventory have been stored. "
                        + "You can resume from this point next time.");
                } else {
                    hubView.displayErrorMessage(
                        "Could not save campaign progress — no active party found yet.");
                }
            } catch (IllegalStateException ex) {
                hubView.displayErrorMessage("Cannot exit during a battle. "
                    + "Finish the current fight first.");
            }
        }
    }

    /**
     * Saves campaign progress (if any) and returns the user to the login screen (logout).
     */
    class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try { campaignService.saveCampaignProgress(currentUser.getUsername()); }
            catch (IllegalStateException ignored) { }
            matchmakingService.removeObserver(currentUser.getUsername());
            java.awt.Point location = hubView.getLocation();
            hubView.dispose();
            com.legends.view.LoginView loginView = new com.legends.view.LoginView("Player");
            loginView.setLocation(location);
            new com.legends.controller.LoginController(loginView);
            loginView.setVisible(true);
        }
    }

    class BackListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            matchmakingService.removeObserver(currentUser.getUsername());
            hubView.dispose();
            if (homeView != null) {
                homeView.setVisible(true);
            }
        }
    }

    // --------------------------------------------------------
    // Observer callbacks — called by MatchmakingService
    // These are triggered from another player's Swing event, so we
    // use invokeLater to schedule our own dialogs independently,
    // breaking the call chain and preventing a Swing deadlock.
    // --------------------------------------------------------

    @Override
    public void onInviteReceived(String senderUsername) {
        // This is called synchronously from the sender's thread — defer to EDT
        SwingUtilities.invokeLater(() -> {
            int response = hubView.showInvitePrompt(senderUsername);
            boolean accepted = (response == JOptionPane.YES_OPTION);

            matchmakingService.respondToInvite(senderUsername, currentUser.getUsername(), accepted);

            if (accepted) {
                // Create a shared MatchContext between the two controllers.
                // The sender's onInviteAccepted will also call handleInvite on their own copy,
                // but we use a shared reference via MatchmakingService to coordinate party IDs.
                currentMatch = matchmakingService.getOrCreateMatch(senderUsername, currentUser.getUsername());
                currentMatch.handleInvite(true);

                // Prompt this player (receiver/opponent) to pick a party
                int partyIndex = promptLocalPartySelection();
                currentMatch.selectParty(currentUser.getUsername(), partyIndex);
                maybeRunBattle(senderUsername, currentUser.getUsername());
            }
        });
    }

    @Override
    public void onInviteAccepted(String receiverUsername) {
        // Deferred so this player's dialog runs after the receiver's dialog completes
        SwingUtilities.invokeLater(() -> {
            hubView.appendStatus(receiverUsername + " accepted your invite! Select your party.");

            // Get the same shared MatchContext the receiver already created
            currentMatch = matchmakingService.getOrCreateMatch(currentUser.getUsername(), receiverUsername);

            // Prompt this player (sender/challenger) to pick a party
            int partyIndex = promptLocalPartySelection();
            currentMatch.selectParty(currentUser.getUsername(), partyIndex);
            maybeRunBattle(currentUser.getUsername(), receiverUsername);
        });
    }

    @Override
    public void onInviteDeclined(String receiverUsername) {
        SwingUtilities.invokeLater(() ->
            hubView.displayErrorMessage(receiverUsername + " declined your invitation.")
        );
    }

    @Override
    public void onMatchCompleted(String summary) {
        SwingUtilities.invokeLater(() ->
            hubView.displaySuccessMessage(summary)
        );
    }

    // --------------------------------------------------------
    // Private helpers
    // --------------------------------------------------------

    private int promptLocalPartySelection() {
        List<Party> parties = partyService.getSavedParties(currentUser.getUsername());
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < parties.size(); i++) {
            Party p = parties.get(i);
            labels.add("Party " + (i + 1) + " — " + p.heroes().size()
                + " hero(es), lvl " + p.cumulativeLevel() + ", gold " + p.gold());
        }
        if (labels.isEmpty()) {
            hubView.displayErrorMessage("You have no saved parties!");
            return 0;
        }
        return hubView.promptPartySelection(labels);
    }

    private void maybeRunBattle(String challenger, String opponent) {
        if (currentMatch == null) return;
        if (currentMatch.getChallengerPartyId() == -1 || currentMatch.getOpponentPartyId() == -1) {
            return;
        }

        try {
            Party challengerParty = AppServices.partyService()
                .getSavedParty(challenger, currentMatch.getChallengerPartyId());
            Party opponentParty = AppServices.partyService()
                .getSavedParty(opponent, currentMatch.getOpponentPartyId());

            if (challengerParty == null || opponentParty == null) {
                hubView.displayErrorMessage("Could not load selected parties.");
                return;
            }

            currentMatch.endMatch();
            matchmakingService.clearMatch(challenger, opponent);

            // Determine screen positions: challenger on left, opponent on right
            int x1 = hubView.getLocation().x;
            int x2 = x1 + 880;

            // Launch two interactive windows — one per player, both human-controlled
            new PvpBattleController(
                challengerParty, opponentParty,
                challenger, opponent,
                x1, x2,
                matchmakingService
            );

        } catch (IllegalStateException ex) {
            hubView.displayErrorMessage("PvP battle could not start: " + ex.getMessage());
        }
    }

    private void handleInnVisit(CampaignService.CampaignStepResult result) {
        List<CampaignService.InnItemOption> items = result.innItems();
        List<String> heroNames = result.party().heroes().stream().map(unit.Hero::name).toList();
        while (!items.isEmpty()) {
            String choice = hubView.promptInnPurchase(items.stream()
                .map(item -> item.name() + " (" + item.cost() + "g, " + item.effect() + ")")
                .toList());
            if (choice == null) break;
            String itemName = choice.substring(0, choice.indexOf(" ("));
            String heroTarget = hubView.promptHeroTarget(heroNames, itemName);
            if (heroTarget == null) break;
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
        if (entries.isEmpty()) return "No completed campaigns yet.";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            CampaignRecordService.HallOfFameEntry entry = entries.get(i);
            builder.append(i + 1).append(". ").append(entry.username()).append(" - ").append(entry.score());
            if (i < entries.size() - 1) builder.append('\n');
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

        // Rebuild post-battle party from live session heroes (not stale pre-battle snapshot)
        // This ensures depleted mana/HP and gained XP are preserved correctly.
        unit.Party postBattleParty = unit.Party.empty();
        postBattleParty.addGold(result.pendingBattle().party().gold());
        for (unit.Unit u : session.players()) {
            if (u instanceof unit.Hero hero) {
                postBattleParty.addHero(hero);
            }
        }

        return campaignService.resolvePendingBattle(
            currentUser.getUsername(),
            outcome.winner() == unit.Team.PLAYER,
            postBattleParty
        );
    }
}
