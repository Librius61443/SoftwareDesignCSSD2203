package com.legends.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Home screen shown after login.
 * Gives the player three clear choices: PvE Campaign, PvP Battle, or Logout.
 */
public class HomeView extends JFrame {

    private final JButton pveButton    = new JButton("PvE Campaign");
    private final JButton pvpButton    = new JButton("PvP Battle");
    private final JButton logoutButton = new JButton("Logout");
    private final JLabel  statusLabel  = new JLabel(" ", SwingConstants.CENTER);

    public HomeView(String username) {
        setTitle("Legends of Sword and Wand");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // Title area
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        JLabel title = new JLabel("Legends of Sword and Wand", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel welcome = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.PLAIN, 13));
        titlePanel.add(title);
        titlePanel.add(welcome);
        add(titlePanel, BorderLayout.NORTH);

        // Button area
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 10, 60));

        pveButton.setFont(new Font("Arial", Font.BOLD, 14));
        pveButton.setPreferredSize(new Dimension(200, 40));
        pvpButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 12));

        buttonPanel.add(pveButton);
        buttonPanel.add(pvpButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public void addPveListener(ActionListener l)    { pveButton.addActionListener(l); }
    public void addPvpListener(ActionListener l)    { pvpButton.addActionListener(l); }
    public void addLogoutListener(ActionListener l) { logoutButton.addActionListener(l); }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
