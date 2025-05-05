import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private JPanel menuPanel;

    public AdminDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);
        
        // Create menu panel
        menuPanel = createMenuPanel();
        splitPane.setLeftComponent(menuPanel);
        
        // Create content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        splitPane.setRightComponent(contentPanel);
        
        // Create status bar
        statusLabel = new JLabel("Welcome, Admin!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);
        
        add(splitPane);
        
        // Show welcome message
        showWelcomeMessage();
        
        // Load pending approvals count
        loadPendingApprovalsCount();
        
        // Apply initial theme
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);

        // Add admin profile section
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        JLabel adminLabel = new JLabel("Administrator");
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        adminLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        profilePanel.add(adminLabel, BorderLayout.CENTER);
        panel.add(profilePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] menuItems = {
            "Dashboard Home",
            "Manage Librarians",
            "View Reports",
            "Fine Management",
            "User Approvals",
            "System Settings",
            "Toggle Theme",
            "Logout"
        };

        for (String item : menuItems) {
            JButton button = createMenuButton(item);
            panel.add(button);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(230, 40));
        button.setPreferredSize(new Dimension(230, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        
        button.addActionListener(e -> {
            switch (text) {
                case "Dashboard Home":
                    showWelcomeMessage();
                    break;
                case "Manage Librarians":
                    showLibrarianManagement();
                    break;
                case "View Reports":
                    showReports();
                    break;
                case "Fine Management":
                    showFineManagement();
                    break;
                case "User Approvals":
                    showUserApprovals();
                    break;
                case "System Settings":
                    showSettings();
                    break;
                case "Toggle Theme":
                    toggleTheme();
                    break;
                case "Logout":
                    logout();
                    break;
            }
        });
        
        return button;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        // Apply theme to menu panel
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        
        // Apply theme to content panel
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        // Update button colors
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            }
        }

        // Update status bar
        statusLabel.setBackground(isDarkMode ? darkBackground : lightBackground);
        statusLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        // Refresh the UI
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Admin Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        welcomePanel.add(welcomeLabel, gbc);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        gbc.gridy = 1;
        welcomePanel.add(statsPanel, gbc);
        
        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Welcome to Dashboard");
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add stat cards
        panel.add(createStatCard("Total Users", getTotalUsers()));
        panel.add(createStatCard("Total Books", getTotalBooks()));
        panel.add(createStatCard("Active Loans", getActiveLoanCount()));
        panel.add(createStatCard("Pending Approvals", getPendingApprovals()));

        return panel;
    }

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(isDarkMode ? new Color(45, 45, 45) : new Color(255, 255, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60, 60, 60) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        
        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(isDarkMode ? new Color(70, 130, 180) : new Color(70, 130, 180));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private void showLibrarianManagement() {
        contentPanel.removeAll();
        LibrarianManagementPanel panel = new LibrarianManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Librarians");
    }

    private void showReports() {
        contentPanel.removeAll();
        ReportsPanel panel = new ReportsPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Viewing Reports");
    }

    private void showFineManagement() {
        contentPanel.removeAll();
        FineManagementPanel panel = new FineManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Fines");
    }

    private void showUserApprovals() {
        contentPanel.removeAll();
        UserApprovalPanel panel = new UserApprovalPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing User Approvals");
    }

    private void showSettings() {
        contentPanel.removeAll();
        SettingsPanel panel = new SettingsPanel(userId);
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("System Settings");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginScreen().setVisible(true);
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void loadPendingApprovalsCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) FROM users WHERE is_active = false";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next() && rs.getInt(1) > 0) {
                updateStatus("You have " + rs.getInt(1) + " pending user approvals");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Database utility methods
    private int getTotalUsers() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE is_active = true");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalBooks() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books WHERE is_active = true");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getActiveLoanCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM book_borrowings WHERE status = 'BORROWED'"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getPendingApprovals() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE is_active = false");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
