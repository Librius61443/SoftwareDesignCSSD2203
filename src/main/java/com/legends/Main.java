package com.legends;

import com.legends.controller.LoginController;
import com.legends.view.LoginView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView loginView1 = new LoginView("Player 1");
            new LoginController(loginView1);
            loginView1.setLocation(100, 200);
            loginView1.setVisible(true);

            LoginView loginView2 = new LoginView("Player 2");
            new LoginController(loginView2);
            loginView2.setLocation(500, 200);
            loginView2.setVisible(true);
        });
    }
}
