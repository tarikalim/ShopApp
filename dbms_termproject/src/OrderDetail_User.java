import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetail_User extends JFrame {
    private JTable orderDetailTable;
    private JButton closeButton, reviewButton;
    private int orderID;
    private int userID;

    public OrderDetail_User(int userID, int orderID) {
        this.userID = userID;
        this.orderID = orderID;

        setTitle("Order Details");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        orderDetailTable = new JTable();
        orderDetailTable.setFont(new Font("Arial", Font.PLAIN, 16));
        orderDetailTable.setRowHeight(30);

        closeButton = createButton("Exit", new Color(0, 0, 128)); // Koyu mavi renkli buton
        closeButton.addActionListener(e -> dispose());

        reviewButton = createButton("Make Review", new Color(0, 0, 128)); // Koyu mavi renkli buton
        reviewButton.addActionListener(e -> initiateReviewProcess());

        JPanel panel = new JPanel();
        panel.add(closeButton);
        panel.add(reviewButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(orderDetailTable), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        showOrderDetails(orderID);
    }

    private void showOrderDetails(int orderID) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ProductID");
        model.addColumn("Product Name");
        model.addColumn("Quantity");
        model.addColumn("SalePrice");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT OD.ProductID, P.Name AS ProductName, OD.Quantity, OD.SalePrice " +
                             "FROM OrderDetails OD " +
                             "INNER JOIN Products P ON OD.ProductID = P.ProductID " +
                             "WHERE OD.OrderID = ?")) {
            preparedStatement.setInt(1, orderID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("ProductID"),
                        resultSet.getString("ProductName"),
                        resultSet.getInt("Quantity"),
                        resultSet.getDouble("SalePrice")
                });
            }
            orderDetailTable.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initiateReviewProcess() {
        String inputProductId = JOptionPane.showInputDialog(this, "Enter the product ID for the review:");
        if (inputProductId != null && !inputProductId.isEmpty()) {
            try {
                int productId = Integer.parseInt(inputProductId);
                if (checkIfProductOrdered(productId)) {
                    String comment = JOptionPane.showInputDialog(this, "Enter your comment:");
                    String rateString = JOptionPane.showInputDialog(this, "Enter your rating (0-5):");
                    int rate = Integer.parseInt(rateString);
                    if (rate < 0 || rate > 5) {
                        JOptionPane.showMessageDialog(this, "Invalid rating. Please enter a value between 0 and 5.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    addReviewToDatabase(productId, comment, rate);
                } else {
                    JOptionPane.showMessageDialog(this, "You cannot review this product as it was not ordered by you.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean checkIfProductOrdered(int productId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM OrderDetails OD " +
                             "INNER JOIN Orders O ON OD.OrderID = O.OrderID " +
                             "WHERE OD.ProductID = ? AND O.UserID = ?")) {
            preparedStatement.setInt(1, productId);
            preparedStatement.setInt(2, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addReviewToDatabase(int productId, String comment, int rate) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO Review (userID, productID, date, comment, rate) VALUES (?, ?, CURDATE(), ?, ?)")) {
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, productId);
            preparedStatement.setString(3, comment);
            preparedStatement.setInt(4, rate);
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Review added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while adding the review.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        return button;
    }


    public static void display(int userID, int orderID) {
        SwingUtilities.invokeLater(() -> {
            OrderDetail_User frame = new OrderDetail_User(userID, orderID);
            frame.setVisible(true);
        });
    }
}
