package com.legends.controller;

import com.legends.dao.UserDAO;
import com.legends.model.User;
import com.legends.view.LoginView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {

    private LoginView loginView;
    private UserDAO userDAO;

    public LoginController(LoginView loginView) {
        this.loginView = loginView;
        this.userDAO = new UserDAO();
        
        this.loginView.addLoginListener(new LoginListener());
        this.loginView.addRegisterListener(new RegisterListener());
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = loginView.getUsername();
            String password = loginView.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                loginView.displayErrorMessage("Username and password cannot be empty.");
                return;
            }

            User authenticatedUser = userDAO.authenticate(username, password);

            if (authenticatedUser != null) {
                loginView.displaySuccessMessage("Login successful!");
                loginView.dispose(); 
            } else {
                loginView.displayErrorMessage("Invalid username or password.");
            }
        }
    }

    class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = loginView.getUsername();
            String password = loginView.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                loginView.displayErrorMessage("Username and password cannot be empty.");
                return;
            }

            if (userDAO.getUser(username) != null) {
                loginView.displayErrorMessage("Username already exists.");
                return;
            }

            User newUser = new User(username, password);
            boolean isCreated = userDAO.createUser(newUser);

            if (isCreated) {
                loginView.displaySuccessMessage("Profile created successfully!");
            } else {
                loginView.displayErrorMessage("Failed to create profile.");
            }
        }
    }
}