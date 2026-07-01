package com.daa.resumescorer.ui;

import com.daa.resumescorer.db.UserDAO;
import com.daa.resumescorer.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Blocking modal dialog. Call showLogin() — it returns the logged-in User,
 * or null if the user closed the window / cancelled.
 */
public class LoginDialog extends JDialog {

    private static final Color BG_DARK  = new Color(13, 17, 23);
    private static final Color BG_INPUT = new Color(33, 38, 45);
    private static final Color ACCENT   = new Color(88, 166, 255);
    private static final Color TEXT_MAIN = new Color(230, 237, 243);
    private static final Color TEXT_DIM  = new Color(139, 148, 158);
    private static final Color CLR_RED   = new Color(248, 81, 73);

    private final UserDAO userDAO = new UserDAO();
    private User result = null;

    private JTextField userField;
    private JPasswordField passField;
    private JLabel msgLabel;

    public LoginDialog(Frame owner) {
        super(owner, "Smart Resume Scorer - Login", true);
        setSize(380, 380);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Smart Resume Scorer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_DIM);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(20));

        userField = makeField();
        passField = new JPasswordField();
        styleField(passField);

        panel.add(labeled("Username", userField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(labeled("Password", passField));
        panel.add(Box.createVerticalStrut(16));

        msgLabel = new JLabel(" ");
        msgLabel.setForeground(CLR_RED);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(msgLabel);
        panel.add(Box.createVerticalStrut(8));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(ACCENT);
        loginBtn.setForeground(BG_DARK);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(9999, 38));
        loginBtn.addActionListener(e -> doLogin());

        JButton registerBtn = new JButton("Create new account");
        registerBtn.setBackground(BG_DARK);
        registerBtn.setForeground(ACCENT);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> doRegister());

        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(registerBtn);

        setContentPane(panel);
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(48, 54, 61), 1, true), new EmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(9999, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_DARK);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(9999, 58));
        JLabel l = new JLabel(label);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            msgLabel.setText("Enter both username and password.");
            return;
        }
        User user = userDAO.login(u, p);
        if (user == null) {
            msgLabel.setText("Invalid username or password.");
            return;
        }
        this.result = user;
        dispose();
    }

    private void doRegister() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            msgLabel.setText("Enter a username and password to register.");
            return;
        }
        if (p.length() < 4) {
            msgLabel.setText("Password should be at least 4 characters.");
            return;
        }
        boolean ok = userDAO.register(u, p);
        if (!ok) {
            msgLabel.setText("That username is already taken.");
            return;
        }
        msgLabel.setForeground(new Color(63, 185, 80));
        msgLabel.setText("Account created! Click Login to continue.");
    }

    /** Shows the dialog and blocks until the user logs in or closes it. */
    public static User showLogin(Frame owner) {
        LoginDialog dlg = new LoginDialog(owner);
        dlg.setVisible(true);
        return dlg.result;
    }
}
