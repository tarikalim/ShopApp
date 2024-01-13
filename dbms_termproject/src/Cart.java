import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Cart extends JFrame {
    private JTable cartDetailsTable;
    private JLabel totalLabel;
    private JTextField productIdToRemoveField;
    private JButton removeProductButton, confirmOrderButton;
    private int userID;

    public Cart(int userID) {
        this.userID = userID;
        setTitle("Cart");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cartDetailsTable = new JTable();
        cartDetailsTable.setFont(new Font("Arial", Font.PLAIN, 16));
        cartDetailsTable.setRowHeight(30);
        totalLabel = new JLabel();
        productIdToRemoveField = new JTextField(10);
        removeProductButton = new JButton("Delete product");
        confirmOrderButton = new JButton("Confirm cart");


        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color backgroundColor = new Color(230, 240, 250);
        Color buttonColor = new Color(0, 0, 128);

        setButtonStyle(removeProductButton, buttonColor, buttonFont);
        setButtonStyle(confirmOrderButton, buttonColor, buttonFont);

        removeProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int productId = Integer.parseInt(productIdToRemoveField.getText());
                    removeProductFromCart(productId);
                    refreshCartDetails();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Provide valid productID.");
                }
            }
        });

        confirmOrderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openUserOrderScreen();
            }
        });

        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.add(new JScrollPane(cartDetailsTable), BorderLayout.CENTER);
        detailsPanel.add(totalLabel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(backgroundColor);
        controlPanel.add(productIdToRemoveField);
        controlPanel.add(removeProductButton);
        controlPanel.add(confirmOrderButton);

        setLayout(new BorderLayout());
        add(detailsPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        refreshCartDetails();
    }

    private void refreshCartDetails() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ProductID");
        model.addColumn("Product Name");
        model.addColumn("Price");
        model.addColumn("Quantity");
        model.addColumn("Total");

        double total = 0;

        String query = "SELECT P.ProductID, P.Name, P.Price, CD.Quantity, (P.Price * CD.Quantity) AS Total " +
                "FROM CartDetails CD " +
                "INNER JOIN Cart C ON CD.CartID = C.CartID " +
                "INNER JOIN Products P ON CD.ProductID = P.ProductID " +
                "WHERE C.UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int productID = rs.getInt("ProductID");
                String productName = rs.getString("Name");
                double price = rs.getDouble("Price");
                int quantity = rs.getInt("Quantity");
                double totalPerProduct = rs.getDouble("Total");

                model.addRow(new Object[]{productID, productName, price, quantity, totalPerProduct});
                total += totalPerProduct;
            }

            cartDetailsTable.setModel(model);
            totalLabel.setText("Cart Total: " + total + " TL");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void removeProductFromCart(int productID) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Sepeti ve ürünün miktarını kontrol et
            String checkCartSql = "SELECT Quantity FROM CartDetails WHERE CartID = (SELECT CartID FROM Cart WHERE UserID = ?) AND ProductID = ?";
            try (PreparedStatement checkCartStmt = connection.prepareStatement(checkCartSql)) {
                checkCartStmt.setInt(1, userID);
                checkCartStmt.setInt(2, productID);
                ResultSet resultSet = checkCartStmt.executeQuery();

                if (resultSet.next()) {
                    int quantity = resultSet.getInt("Quantity");
                    if (quantity > 1) {
                        // Miktarı 1 azalt
                        String updateCartSql = "UPDATE CartDetails SET Quantity = Quantity - 1 WHERE CartID = (SELECT CartID FROM Cart WHERE UserID = ?) AND ProductID = ?";
                        try (PreparedStatement updateCartStmt = connection.prepareStatement(updateCartSql)) {
                            updateCartStmt.setInt(1, userID);
                            updateCartStmt.setInt(2, productID);
                            updateCartStmt.executeUpdate();
                        }
                    } else {
                        // Ürünü sepetten çıkar
                        String deleteCartSql = "DELETE FROM CartDetails WHERE CartID = (SELECT CartID FROM Cart WHERE UserID = ?) AND ProductID = ?";
                        try (PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartSql)) {
                            deleteCartStmt.setInt(1, userID);
                            deleteCartStmt.setInt(2, productID);
                            deleteCartStmt.executeUpdate();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "This product is not available in your cart.\n.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting product.");
        }
    }

    private void setButtonStyle(JButton button, Color buttonColor, Font buttonFont) {
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(buttonFont);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

    private void openUserOrderScreen() {
        UserOrder userOrder = new UserOrder(userID);
        userOrder.setVisible(true);
    }
}

