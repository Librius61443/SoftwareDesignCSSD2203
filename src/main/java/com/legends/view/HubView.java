package com.legends.view;

import unit.HeroClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class HubView extends JFrame {

    private JTextField inviteField;
    private JButton inviteButton;
    private JButton playNextRoomButton;
    private JButton savePartyButton;
    private JButton viewPartyButton;
    private JButton hallOfFameButton;
    private JComboBox<HeroClass> classSelector;
    private JLabel campaignProgressLabel;
    private JTextArea statusArea;

    public HubView(String username) {
        setTitle("Legends of Sword and Wand - Hub");
        setSize(650, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel campaignPanel = new JPanel(new FlowLayout());
        campaignPanel.add(new JLabel("Class Focus:"));
        classSelector = new JComboBox<>(HeroClass.values());
        campaignPanel.add(classSelector);
        playNextRoomButton = new JButton("Play Next Room");
        savePartyButton = new JButton("Save Active Party");
        viewPartyButton = new JButton("View Party Status");
        hallOfFameButton = new JButton("View Hall of Fame");
        campaignPanel.add(playNextRoomButton);
        campaignPanel.add(savePartyButton);
        campaignPanel.add(viewPartyButton);
        campaignPanel.add(hallOfFameButton);
        centerPanel.add(campaignPanel, BorderLayout.NORTH);

        JPanel pvpPanel = new JPanel(new FlowLayout());
        pvpPanel.add(new JLabel("Invite User to PvP:"));
        inviteField = new JTextField(15);
        pvpPanel.add(inviteField);
        inviteButton = new JButton("Send Invite");
        pvpPanel.add(inviteButton);
        centerPanel.add(pvpPanel, BorderLayout.CENTER);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setMargin(new Insets(10, 10, 10, 10));

        JPanel statusPanel = new JPanel(new BorderLayout(8, 8));
        campaignProgressLabel = new JLabel("Campaign progress: room 1 of 30");
        statusPanel.add(campaignProgressLabel, BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        centerPanel.add(statusPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    public String getInviteUsername() {
        return inviteField.getText().trim();
    }

    public void addSendInviteListener(ActionListener listener) {
        inviteButton.addActionListener(listener);
    }

    public void addRunCampaignListener(ActionListener listener) {
        playNextRoomButton.addActionListener(listener);
    }

    public void addSavePartyListener(ActionListener listener) {
        savePartyButton.addActionListener(listener);
    }

    public void addViewPartyListener(ActionListener listener) {
        viewPartyButton.addActionListener(listener);
    }

    public void addViewHallOfFameListener(ActionListener listener) {
        hallOfFameButton.addActionListener(listener);
    }

    public void displayErrorMessage(String message) {
        appendStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displaySuccessMessage(String message) {
        appendStatus(message);
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void appendStatus(String message) {
        if (!statusArea.getText().isEmpty()) {
            statusArea.append("\n\n");
        }
        statusArea.append(message);
    }

    public int showInvitePrompt(String senderUsername) {
        return JOptionPane.showConfirmDialog(
            this,
            senderUsername + " has invited you to a PvP battle! Do you accept?",
            "PvP Invitation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
    }

    public HeroClass getSelectedHeroClass() {
        return (HeroClass) classSelector.getSelectedItem();
    }

    public void setCampaignProgress(int nextRoom, boolean completed) {
        campaignProgressLabel.setText(
            completed ? "Campaign progress: completed" : "Campaign progress: room " + nextRoom + " of 30"
        );
    }

    public String promptInnPurchase(java.util.List<String> itemLabels) {
        Object selection = JOptionPane.showInputDialog(
            this,
            "Choose an inn item to buy, or cancel to leave the shop.",
            "Inn Shop",
            JOptionPane.PLAIN_MESSAGE,
            null,
            itemLabels.toArray(),
            itemLabels.isEmpty() ? null : itemLabels.get(0)
        );
        return selection == null ? null : selection.toString();
    }

    public String promptHeroTarget(java.util.List<String> heroLabels, String itemName) {
        Object selection = JOptionPane.showInputDialog(
            this,
            "Choose a hero to receive " + itemName + ".",
            "Inn Item Target",
            JOptionPane.PLAIN_MESSAGE,
            null,
            heroLabels.toArray(),
            heroLabels.isEmpty() ? null : heroLabels.get(0)
        );
        return selection == null ? null : selection.toString();
    }

    public boolean confirmRecruit(String recruitLabel) {
        return JOptionPane.showConfirmDialog(
            this,
            recruitLabel + "\nRecruit this hero?",
            "Inn Recruit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
}
