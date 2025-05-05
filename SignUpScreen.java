import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox;

    public SignUpScreen() {
        setTitle("Library Management System - Sign Up");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Full Name
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        fullNameField = new JTextField(20);
        mainPanel.add(fullNameField, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        emailField = new JTextField(20);
        mainPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);

        // Role Selection
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("Role:"), gbc);
        
        gbc.gridx = 1;
        String[] roles = {"Student", "Librarian"};  // Admin accounts should be created manually
        roleComboBox = new JComboBox<>(roles);
        mainPanel.add(roleComboBox, gbc);

        // Sign Up Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(e -> handleSignUp());
        mainPanel.add(signUpButton, gbc);

        // Back to Login Button
        gbc.gridy = 8;
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            this.dispose();
        });
        mainPanel.add(backButton, gbc);

        add(mainPanel);
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if username already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            // Check if email already exists
            checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Email already registered!");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, email, full_name, role, is_active) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, role.toUpperCase());
            pstmt.setBoolean(6, false);  // Account needs admin approval
            
            pstmt.executeUpdate();

            // Create notification for admin
            String notifyQuery = "INSERT INTO notifications (user_id, message, type) " +
                               "SELECT user_id, ?, 'APPROVAL' FROM users WHERE role = 'ADMIN'";
            PreparedStatement notifyStmt = conn.prepareStatement(notifyQuery);
            notifyStmt.setString(1, "New " + role + " account registration: " + username);
            notifyStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, 
                "Account created successfully!\nPlease wait for admin approval to login.");
            
            // Return to login screen
            new LoginScreen().setVisible(true);
            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
