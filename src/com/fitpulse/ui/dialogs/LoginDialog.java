package com.fitpulse.ui.dialogs;

import com.fitpulse.model.User;
import com.fitpulse.service.UserService;
import com.fitpulse.ui.components.ModernButton;
import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * Modern modal dialog for User Authentication and Registration.
 */
public class LoginDialog extends JDialog {
    private final UserService userService;
    private User authenticatedUser = null;

    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPassword;

    // Registration inputs
    private JTextField txtRegName;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;
    private JTextField txtRegWeight;
    private JTextField txtRegHeight;
    private JComboBox<String> cmbFitnessLevel;

    public LoginDialog(Frame parent, UserService userService) {
        super(parent, "FitPulse — Sign In / Register", true);
        this.userService = userService;
        initUI();
    }

    private void initUI() {
        setSize(480, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_MAIN);

        tabbedPane.addTab("Sign In", createSignInPanel());
        tabbedPane.addTab("Create Member Account", createRegisterPanel());

        add(tabbedPane);
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Welcome Back to FitPulse", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 0;
        panel.add(lblTitle, gbc);

        JLabel lblHint = new JLabel("Demo email: sarah@fitpulse.com | Pass: password123", SwingConstants.CENTER);
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.ACCENT_TEAL);
        gbc.gridy = 1;
        panel.add(lblHint, gbc);

        // Email
        gbc.gridy = 2;
        panel.add(createFieldLabel("Email Address"), gbc);
        txtLoginEmail = createTextField("sarah@fitpulse.com");
        gbc.gridy = 3;
        panel.add(txtLoginEmail, gbc);

        // Password
        gbc.gridy = 4;
        panel.add(createFieldLabel("Password"), gbc);
        txtLoginPassword = new JPasswordField("password123");
        styleInput(txtLoginPassword);
        gbc.gridy = 5;
        panel.add(txtLoginPassword, gbc);

        // Login Button
        ModernButton btnLogin = new ModernButton("Sign In", UITheme.ACCENT_PRIMARY);
        btnLogin.addActionListener(e -> performLogin());
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnLogin, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel lblTitle = new JLabel("Join FitPulse Today", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
        panel.add(lblTitle, gbc);

        gbc.gridy = row++;
        panel.add(createFieldLabel("Full Name"), gbc);
        txtRegName = createTextField("");
        gbc.gridy = row++;
        panel.add(txtRegName, gbc);

        gbc.gridy = row++;
        panel.add(createFieldLabel("Email"), gbc);
        txtRegEmail = createTextField("");
        gbc.gridy = row++;
        panel.add(txtRegEmail, gbc);

        gbc.gridy = row++;
        panel.add(createFieldLabel("Password"), gbc);
        txtRegPassword = new JPasswordField();
        styleInput(txtRegPassword);
        gbc.gridy = row++;
        panel.add(txtRegPassword, gbc);

        // Weight & Height
        JPanel numPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        numPanel.setOpaque(false);
        txtRegWeight = createTextField("70.0");
        txtRegHeight = createTextField("175.0");

        JPanel wP = new JPanel(new BorderLayout(0, 4));
        wP.setOpaque(false);
        wP.add(createFieldLabel("Weight (kg)"), BorderLayout.NORTH);
        wP.add(txtRegWeight, BorderLayout.CENTER);

        JPanel hP = new JPanel(new BorderLayout(0, 4));
        hP.setOpaque(false);
        hP.add(createFieldLabel("Height (cm)"), BorderLayout.NORTH);
        hP.add(txtRegHeight, BorderLayout.CENTER);

        numPanel.add(wP);
        numPanel.add(hP);

        gbc.gridy = row++;
        panel.add(numPanel, gbc);

        // Fitness Level
        gbc.gridy = row++;
        panel.add(createFieldLabel("Fitness Experience"), gbc);
        cmbFitnessLevel = new JComboBox<>(new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"});
        cmbFitnessLevel.setFont(UITheme.FONT_BODY);
        cmbFitnessLevel.setBackground(UITheme.BG_INPUT);
        cmbFitnessLevel.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
        panel.add(cmbFitnessLevel, gbc);

        // Register Button
        ModernButton btnRegister = new ModernButton("Create Account", UITheme.ACCENT_TEAL);
        btnRegister.addActionListener(e -> performRegister());
        gbc.gridy = row++;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(btnRegister, gbc);

        return panel;
    }

    private void performLogin() {
        String email = txtLoginEmail.getText().trim();
        String pass = new String(txtLoginPassword.getPassword());

        try {
            this.authenticatedUser = userService.authenticate(email, pass);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRegister() {
        try {
            String name = txtRegName.getText().trim();
            String email = txtRegEmail.getText().trim();
            String pass = new String(txtRegPassword.getPassword());
            double weight = Double.parseDouble(txtRegWeight.getText().trim());
            double height = Double.parseDouble(txtRegHeight.getText().trim());
            String level = (String) cmbFitnessLevel.getSelectedItem();

            this.authenticatedUser = userService.registerMember(name, email, pass, weight, height, level);
            JOptionPane.showMessageDialog(this, "Welcome to FitPulse, " + authenticatedUser.getName() + "!",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for weight and height.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SMALL);
        label.setForeground(UITheme.TEXT_MUTED);
        return label;
    }

    private JTextField createTextField(String text) {
        JTextField tf = new JTextField(text);
        styleInput(tf);
        return tf;
    }

    private void styleInput(JTextField field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBackground(UITheme.BG_INPUT);
        field.setForeground(UITheme.TEXT_MAIN);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
