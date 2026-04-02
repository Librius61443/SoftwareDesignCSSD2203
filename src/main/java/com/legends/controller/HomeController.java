package com.legends.controller;

import com.legends.model.User;
import com.legends.service.AppServices;
import com.legends.service.CampaignService;
import com.legends.service.MatchmakingService;
import com.legends.view.HomeView;
import com.legends.view.HubView;
import com.legends.view.LoginView;
import com.legends.view.PveView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controls the home screen. Launches PvE or PvP sub-screens and handles logout.
 */
public class HomeController {

    private final HomeView           homeView;
    private final User               user;
    private final MatchmakingService matchmakingService;
    private final CampaignService    campaignService;

    public HomeController(HomeView homeView, User user, MatchmakingService matchmakingService) {
        this.homeView           = homeView;
        this.user               = user;
        this.matchmakingService = matchmakingService;
        this.campaignService    = AppServices.campaignService();

        homeView.addPveListener(new PveListener());
        homeView.addPvpListener(new PvpListener());
        homeView.addLogoutListener(new LogoutListener());

        // Show current campaign progress in status bar
        int nextRoom = campaignService.getNextRoomNumber(user.getUsername());
        boolean completed = campaignService.isCampaignCompleted(user.getUsername());
        homeView.setStatus(completed
            ? "Campaign completed!"
            : (nextRoom > 1 ? "Campaign in progress — room " + nextRoom + " of 30" : "Ready to play"));
    }

    class PveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            homeView.setVisible(false);
            PveView pveView = new PveView(user.getUsername());
            new PveController(pveView, homeView, user);
            pveView.setVisible(true);
        }
    }

    class PvpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            homeView.setVisible(false);
            HubView hubView = new HubView(user.getUsername());
            hubView.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            hubView.setLocation(homeView.getLocation());
            // Re-use PVPController which handles all matchmaking logic
            new PVPController(hubView, user, matchmakingService, homeView);
            hubView.setVisible(true);
        }
    }

    class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            matchmakingService.removeObserver(user.getUsername());
            java.awt.Point loc = homeView.getLocation();
            homeView.dispose();
            LoginView loginView = new LoginView("Player");
            loginView.setLocation(loc);
            new LoginController(loginView);
            loginView.setVisible(true);
        }
    }
}
