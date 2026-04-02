package com.legends.view;

import unit.HeroClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dedicated PvE campaign screen.
 * Shows party status, campaign progress, and all campaign actions.
 */
public class PveView extends JFrame {

    private final JComboBox<HeroClass> classSelector    = new JComboBox<>(HeroClass.values());
    private final JButton nextRoomButton                 = new JButton("Next Room");
    private final JButton savePartyButton                = new JButton("Save Party for PvP");
    private final JButton viewPartyButton                = new JButton("View Party");
    private final JButton hallOfFameButton               = new JButton("Hall of Fame");
    private final JButton exitButton                     = new JButton("← Back to Home");
    private final JLabel  progressLabel                  = new JLabel("Room 1 of 30", SwingConstants.CENTER);
    private final JTextArea statusArea                   = new JTextArea();

    public PveView(String username) {
        setTitle("PvE Campaign — " + username);
        setSize(680, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ── Top bar ──────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel titleLabel = new JLabel("PvE Campaign", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        topBar.add(titleLabel, BorderLayout.WEST);

        progressLabel.setFont(new Font("Arial", Font.BOLD, 13));
        progressLabel.setForeground(new Color(0, 100, 0));
        topBar.add(progressLabel, BorderLayout.CENTER);

        topBar.add(exitButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Control row ──────────────────────────────────────────
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        controlRow.add(new JLabel("Class:"));
        controlRow.add(classSelector);
        nextRoomButton.setFont(new Font("Arial", Font.BOLD, 13));
        controlRow.add(nextRoomButton);
        controlRow.add(savePartyButton);
        controlRow.add(viewPartyButton);
        controlRow.add(hallOfFameButton);

        // ── Status area ──────────────────────────────────────────
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(statusArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Campaign Log"));

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        center.add(controlRow, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ── Getters ──────────────────────────────────────────────────

    public HeroClass getSelectedClass() {
        return (HeroClass) classSelector.getSelectedItem();
    }

    // ── Listeners ────────────────────────────────────────────────

    public void addNextRoomListener(ActionListener l)   { nextRoomButton.addActionListener(l); }
    public void addSavePartyListener(ActionListener l)  { savePartyButton.addActionListener(l); }
    public void addViewPartyListener(ActionListener l)  { viewPartyButton.addActionListener(l); }
    public void addHallOfFameListener(ActionListener l) { hallOfFameButton.addActionListener(l); }
    public void addExitListener(ActionListener l)       { exitButton.addActionListener(l); }

    // ── Display helpers ──────────────────────────────────────────

    public void setProgress(int nextRoom, boolean completed) {
        progressLabel.setText(completed ? "Campaign Complete!" : "Room " + nextRoom + " of 30");
        progressLabel.setForeground(completed ? new Color(150, 0, 0) : new Color(0, 100, 0));
    }

    public void log(String message) {
        if (!statusArea.getText().isEmpty()) statusArea.append("\n");
        statusArea.append(message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    public void showError(String message) {
        log("[ERROR] " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        log(message);
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Prompts (inn, recruit) ────────────────────────────────────

    public String promptInnPurchase(List<String> itemLabels) {
        if (itemLabels.isEmpty()) return null;
        Object sel = JOptionPane.showInputDialog(this,
            "Choose an item to buy (Cancel to leave shop):", "Inn Shop",
            JOptionPane.PLAIN_MESSAGE, null, itemLabels.toArray(), itemLabels.get(0));
        return sel == null ? null : sel.toString();
    }

    public String promptHeroTarget(List<String> heroNames, String itemName) {
        if (heroNames.isEmpty()) return null;
        Object sel = JOptionPane.showInputDialog(this,
            "Give \"" + itemName + "\" to:", "Choose Hero",
            JOptionPane.PLAIN_MESSAGE, null, heroNames.toArray(), heroNames.get(0));
        return sel == null ? null : sel.toString();
    }

    public boolean confirmRecruit(String recruitLabel) {
        return JOptionPane.showConfirmDialog(this,
            recruitLabel + "\nRecruit this hero?", "Inn Recruit",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
