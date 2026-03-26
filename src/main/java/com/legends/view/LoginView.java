package com.legends.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginView() {
        setTitle("Legends of Sword and Wand - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inputPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        inputPanel.add(passwordField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------
    // Methods used by the LoginController to get data
    // --------------------------------------------------------
    
    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // --------------------------------------------------------
    // Methods used by the LoginController to listen for clicks
    // --------------------------------------------------------
    
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
    }

    // --------------------------------------------------------
    // UI Feedback Methods
    // --------------------------------------------------------
    
    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displaySuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}