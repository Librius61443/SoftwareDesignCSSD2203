package com.legends.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class HubView extends JFrame {

    private JTextField inviteField;
    private JButton inviteButton;

    public HubView(String username) {
        setTitle("Legends of Sword and Wand - Hub");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel pvpPanel = new JPanel();
        pvpPanel.setLayout(new FlowLayout());
        
        pvpPanel.add(new JLabel("Invite User to PvP:"));
        inviteField = new JTextField(15);
        pvpPanel.add(inviteField);
        
        inviteButton = new JButton("Send Invite");
        pvpPanel.add(inviteButton);
        
        add(pvpPanel, BorderLayout.CENTER);
    }

    public String getInviteUsername() {
        return inviteField.getText().trim();
    }

    public void addSendInviteListener(ActionListener listener) {
        inviteButton.addActionListener(listener);
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displaySuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
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
}