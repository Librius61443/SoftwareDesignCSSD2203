package com.legends.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * PvP lobby screen.
 */
public class HubView extends JFrame {

    private final JTextField  inviteField  = new JTextField(16);
    private final JButton     inviteButton = new JButton("Send Invite");
    private final JButton     backButton   = new JButton("← Back to Home");
    private final JTextArea   statusArea   = new JTextArea();

    public HubView(String username) {
        setTitle("PvP Lobby — " + username);
        setSize(500, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("PvP Lobby", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        topBar.add(title, BorderLayout.WEST);
        topBar.add(backButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel invitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        invitePanel.setBorder(BorderFactory.createTitledBorder("Challenge a Player"));
        invitePanel.add(new JLabel("Username:"));
        invitePanel.add(inviteField);
        invitePanel.add(inviteButton);

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setMargin(new Insets(6, 6, 6, 6));
        JScrollPane scroll = new JScrollPane(statusArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Match Log"));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        center.add(invitePanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void addSendInviteListener(ActionListener l)  { inviteButton.addActionListener(l); }
    public void addBackListener(ActionListener l)         { backButton.addActionListener(l); }

    // Stub listeners kept for PVPController compatibility
    public void addRunCampaignListener(ActionListener l)   { }
    public void addSavePartyListener(ActionListener l)     { }
    public void addViewPartyListener(ActionListener l)     { }
    public void addViewHallOfFameListener(ActionListener l){ }
    public void addExitCampaignListener(ActionListener l)  { }
    public void addLogoutListener(ActionListener l)        { }

    public String getInviteUsername() { return inviteField.getText().trim(); }

    public void appendStatus(String message) {
        if (!statusArea.getText().isEmpty()) statusArea.append("\n");
        statusArea.append(message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    public void displayErrorMessage(String message) {
        appendStatus("[ERROR] " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displaySuccessMessage(String message) {
        appendStatus(message);
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setCampaignProgress(int nextRoom, boolean completed) { }

    public int showInvitePrompt(String senderUsername) {
        return JOptionPane.showConfirmDialog(this,
            senderUsername + " has invited you to a PvP battle! Accept?",
            "PvP Invitation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public int promptPartySelection(List<String> partyLabels) {
        if (partyLabels.isEmpty()) return 0;
        Object[] options = partyLabels.toArray();
        Object selected = JOptionPane.showInputDialog(this,
            "Select your PvP party:", "Party Selection",
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == null) return 0;
        return partyLabels.indexOf(selected.toString());
    }

    public unit.HeroClass getSelectedHeroClass() { return unit.HeroClass.WARRIOR; }
    public String promptInnPurchase(List<String> items) { return null; }
    public String promptHeroTarget(List<String> heroes, String item) { return null; }
    public boolean confirmRecruit(String label) { return false; }
}
