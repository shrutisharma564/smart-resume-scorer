package com.daa.resumescorer;

import com.daa.resumescorer.db.DBConnection;
import com.daa.resumescorer.model.User;
import com.daa.resumescorer.ui.LoginDialog;
import com.daa.resumescorer.ui.SmartResumeScorer;
import com.daa.resumescorer.db.UserDAO;


import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!DBConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL.\n\n"
                    + "1. Make sure MySQL server is running\n"
                    + "2. Make sure you ran sql/schema.sql\n"
                    + "3. Check db.properties (url / user / password)\n\n"
                    + "See README.md for full setup steps.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            UserDAO userDAO = new UserDAO();

            if (!userDAO.anyUserExists()) {
                userDAO.register("admin", "admin123");
            }
            User user = LoginDialog.showLogin(null);
            if (user == null) {
                System.exit(0);
            }
            new SmartResumeScorer(user);
        });
    }
}
