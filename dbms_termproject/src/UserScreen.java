import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserScreen extends JFrame {
    private int userID;
    private JButton viewProductsButton;
    private JButton viewOrdersButton;
    private JButton addressConfigButton;
    private JButton creditCardButton;

    public UserScreen(int userID) {
        this.userID = userID;

        setTitle("User");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 255, 255));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(218, 218, 218));

        viewProductsButton = createButton("See Products", new Color(0, 0, 128),"C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\producticon.png");
        viewOrdersButton = createButton("View my orders", new Color(0, 0, 128),"C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\ordericon.png");
        addressConfigButton = createButton("Address Config", new Color(0, 0, 128),"C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\adressicon.png");
        creditCardButton = createButton("Credit Card Config", new Color(0, 0, 128),"C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\cardicon.png");

        viewProductsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ProductList_User productListScreen = new ProductList_User(userID);
                productListScreen.setVisible(true);
            }
        });

        viewOrdersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OrderList_User orderListScreen = new OrderList_User(userID);
                orderListScreen.setVisible(true);
            }
        });

        addressConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureAddress();
            }
        });
        creditCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureCreditCard();
            }
        });

        buttonPanel.add(viewProductsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(viewOrdersButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addressConfigButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(creditCardButton);

        add(buttonPanel, BorderLayout.WEST);
        setLocationRelativeTo(null);
    }

    private JButton createButton(String text, Color color, String iconPath) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 80));

        // İkonu ekle
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(30, 30,  java.awt.Image.SCALE_SMOOTH); // İkonun boyutunu ayarlayın
        icon = new ImageIcon(newimg);
        button.setIcon(icon);

        return button;
    }

    private void configureAddress() {
        try {
            String currentAddress = getCurrentAddress();

            if (currentAddress == null) {
                String newAddress = JOptionPane.showInputDialog("Please enter your address:");
                if (newAddress != null && !newAddress.isEmpty()) {
                    saveNewAddress(newAddress);
                    JOptionPane.showMessageDialog(null, "Address added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid address.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String updatedAddress = JOptionPane.showInputDialog("Your current address is:\n" + currentAddress + "\nPlease enter the updated address:");
                if (updatedAddress != null) {
                    updateAddress(updatedAddress);
                    JOptionPane.showMessageDialog(null, "Address updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error configuring address.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCurrentAddress() throws SQLException {
        String currentAddress = null;
        String query = "SELECT Address FROM Users WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currentAddress = resultSet.getString("Address");
            }
        }
        return currentAddress;
    }

    private void saveNewAddress(String newAddress) throws SQLException {
        String query = "UPDATE Users SET Address = ? WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newAddress);
            preparedStatement.setInt(2, userID);
            preparedStatement.executeUpdate();
        }
    }

    private void updateAddress(String updatedAddress) throws SQLException {
        String query = "UPDATE Users SET Address = ? WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, updatedAddress);
            preparedStatement.setInt(2, userID);
            preparedStatement.executeUpdate();
        }
    }

    private void configureCreditCard() {
        try {

            String currentCreditCardID = getCurrentCreditCard();

            if (currentCreditCardID != null) {
                JOptionPane.showMessageDialog(null, "Your current Credit Card ID is: " + currentCreditCardID, "Current Credit Card", JOptionPane.INFORMATION_MESSAGE);
            }
            String inputCreditCardID = JOptionPane.showInputDialog("Enter new Credit Card ID (16 digits):");

            if (isValidCreditCardID(inputCreditCardID)) {
                updateCreditCard(inputCreditCardID);
                JOptionPane.showMessageDialog(null, "Credit Card ID updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credit Card ID (must be 16 digits).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error configuring Credit Card ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCurrentCreditCard() throws SQLException {
        String currentCreditCardID = null;
        String query = "SELECT CreditCardID FROM Users WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                currentCreditCardID = resultSet.getString("CreditCardID");
            }
        }
        return currentCreditCardID;
    }

    private boolean isValidCreditCardID(String creditCardID) {
        return creditCardID != null && creditCardID.matches("\\d{16}");
    }

    private void updateCreditCard(String creditCardID) throws SQLException {
        String query = "UPDATE Users SET CreditCardID = ? WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, creditCardID);
            preparedStatement.setInt(2, userID);
            preparedStatement.executeUpdate();
        }
    }


    public static void display(int userID) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UserScreen userScreen = new UserScreen(userID);
                userScreen.setVisible(true);
            }
        });
    }
}
