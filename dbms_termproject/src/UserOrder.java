import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UserOrder extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private int userID;

    public UserOrder(int userID) {
        this.userID = userID;
        setTitle("Confirm Order");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmButton = new JButton("Confirm");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = String.valueOf(passwordField.getPassword());

                // Kullanıcı adı ve şifreyi doğrula
                if (authenticateUser(username, password)) {
                    String paymentType = askPaymentType(); // Ödeme türünü al

                    if (paymentType != null) {
                        int orderID = createOrder(paymentType);
                        if (orderID != -1) {
                            addCartToOrderDetails(orderID);
                            updateProductStock(orderID);
                            clearCart();

                            JOptionPane.showMessageDialog(null, "Order created. Order ID: " + orderID);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "An error occurred while creating the order.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid password or User name.");
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("User Name: "));
        panel.add(usernameField);
        panel.add(new JLabel("Password: "));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(confirmButton);

        add(panel);

        setLocationRelativeTo(null);
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM Users WHERE UserID = ? AND Username = ? AND Password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            pstmt.setString(2, username);
            pstmt.setString(3, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String askPaymentType() {
        Object[] options = {"Pay at the Door", "Credit Card"};
        int paymentChoice = JOptionPane.showOptionDialog(null, "Choose payment type:", "Payment Type", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (paymentChoice == JOptionPane.YES_OPTION) {
            return "Pay at the Door";
        } else if (paymentChoice == JOptionPane.NO_OPTION) {
            // Kredi kartı ödeme seçildiğinde kullanıcının CreditCardID'sini kontrol et
            if (isCreditCardIDNull(userID)) {
                JOptionPane.showMessageDialog(null, "You need to enter a credit card before selecting this payment method.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return askPaymentType(); // Kullanıcıya tekrar ödeme türü seçtir
            } else {
                return "Credit Card";
            }
        } else {
            return null; // Kullanıcı iptal etti
        }
    }

    private boolean isCreditCardIDNull(int userID) {
        String query = "SELECT CreditCardID FROM Users WHERE UserID = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userID);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String creditCardID = rs.getString("CreditCardID");
                return creditCardID == null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int createOrder(String paymentType) {
        String insertOrderQuery = "INSERT INTO Orders (UserID, OrderDate, Address, PaymentType) VALUES (?, ?, ?, ?)";

        // Kullanıcının o anki adresini al
        String userAddress = getUserAddress(userID);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertOrderStmt.setInt(1, userID);
            insertOrderStmt.setDate(2, new Date(System.currentTimeMillis()));

            // Kullanıcının o anki adresini ekleyin
            if (userAddress != null) {
                insertOrderStmt.setString(3, userAddress);
            } else {
                insertOrderStmt.setNull(3, Types.VARCHAR);
            }

            // Ödeme türünü ekleyin
            insertOrderStmt.setString(4, paymentType);

            insertOrderStmt.executeUpdate();

            // Oluşturulan siparişin ID'sini al
            ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    private String getUserAddress(int userID) {
        String address = null;
        String query = "SELECT Address FROM Users WHERE UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                address = rs.getString("Address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return address;
    }


    private void clearCart() {
        String deleteCartDetailsQuery = "DELETE FROM CartDetails WHERE CartID IN (SELECT CartID FROM Cart WHERE UserID = ?)";
        String deleteCartQuery = "DELETE FROM Cart WHERE UserID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement deleteCartDetailsStmt = connection.prepareStatement(deleteCartDetailsQuery);
             PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartQuery)) {
            deleteCartDetailsStmt.setInt(1, userID);
            deleteCartStmt.setInt(1, userID);
            deleteCartDetailsStmt.executeUpdate();
            deleteCartStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCartToOrderDetails(int orderID) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertOrderDetailsQuery = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity) " +
                    "SELECT ?, CD.ProductID, CD.Quantity " +
                    "FROM CartDetails CD " +
                    "WHERE CD.CartID IN (SELECT CartID FROM Cart WHERE UserID = ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(insertOrderDetailsQuery)) {
                pstmt.setInt(1, orderID);
                pstmt.setInt(2, userID);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateProductStock(int orderID) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateStockQuery = "UPDATE Products p " +
                    "INNER JOIN OrderDetails od ON p.ProductID = od.ProductID " +
                    "SET p.StockQuantity = p.StockQuantity - od.Quantity " +
                    "WHERE od.OrderID = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(updateStockQuery)) {
                pstmt.setInt(1, orderID);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}




