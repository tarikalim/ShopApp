import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderList_User extends JFrame {
    private int userID;
    private JTable orderListTable;
    private JScrollPane scrollPane;
    private JButton cancelOrderButton;

    public OrderList_User(int userID) {
        this.userID = userID;

        setTitle("Orders");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        orderListTable = new JTable();
        orderListTable.setFont(new Font("Arial", Font.PLAIN, 16));
        orderListTable.setRowHeight(30);
        scrollPane = new JScrollPane(orderListTable);

        loadUserOrders();

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color backgroundColor = new Color(230, 240, 250);
        Color buttonColor = new Color(0, 0, 128);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        cancelOrderButton = new JButton("Cancel Order");
        setButtonStyle(cancelOrderButton, buttonColor, buttonFont);
        panel.add(cancelOrderButton, BorderLayout.NORTH);
        cancelOrderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelOrder();
            }
        });

        JButton viewOrderDetailsButton = new JButton("View Order Details");
        setButtonStyle(viewOrderDetailsButton, buttonColor, buttonFont);
        panel.add(viewOrderDetailsButton, BorderLayout.SOUTH);
        viewOrderDetailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String orderIDString = JOptionPane.showInputDialog("Enter OrderID to see details:");
                try {
                    int orderID = Integer.parseInt(orderIDString);
                    OrderDetail_User.display(userID, orderID);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid OrderID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        setLocationRelativeTo(null);
    }

    private void loadUserOrders() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("OrderID");
        model.addColumn("Address");
        model.addColumn("OrderDate");
        model.addColumn("TotalAmount");
        model.addColumn("Status");
        model.addColumn("PaymentType");



        String sql = "SELECT Orders.OrderID, Orders.Address, Orders.OrderDate, SUM(OrderDetails.SalePrice) AS TotalAmount, Orders.Status, Orders.PaymentType " +
                "FROM Orders " +
                "INNER JOIN Users ON Orders.UserID = Users.UserID " +
                "LEFT JOIN OrderDetails ON Orders.OrderID = OrderDetails.OrderID " +
                "WHERE Orders.UserID = ? " +
                "GROUP BY Orders.OrderID";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int orderID = rs.getInt("OrderID");
                String address = rs.getString("Address");
                String orderDate = rs.getString("OrderDate");
                double totalAmount = rs.getDouble("TotalAmount");
                String status = rs.getString("Status");
                String paymentType = rs.getString("PaymentType");


                model.addRow(new Object[]{orderID, address, orderDate, totalAmount, status, paymentType});
            }

            orderListTable.setModel(model);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelOrder() {
        String orderIDString = JOptionPane.showInputDialog("To cancel the order, enter the orderID:");
        try {
            int orderID = Integer.parseInt(orderIDString);

            String deleteOrderDetailsQuery = "DELETE FROM OrderDetails WHERE OrderID = ?";
            String deleteOrderQuery = "DELETE FROM Orders WHERE OrderID = ? AND UserID = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement deleteOrderDetailsStatement = conn.prepareStatement(deleteOrderDetailsQuery);
                 PreparedStatement deleteOrderStatement = conn.prepareStatement(deleteOrderQuery)) {

                deleteOrderDetailsStatement.setInt(1, orderID);
                deleteOrderDetailsStatement.executeUpdate();

                deleteOrderStatement.setInt(1, orderID);
                deleteOrderStatement.setInt(2, userID);
                int rowsAffected = deleteOrderStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Order cancelled.", "Successful", JOptionPane.INFORMATION_MESSAGE);
                    loadUserOrders();
                } else {
                    JOptionPane.showMessageDialog(null, "Order didn't cancel.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid orderID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Order didn't cancel.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setButtonStyle(JButton button, Color buttonColor, Font buttonFont) {
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(buttonFont);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

    public static void display(int userID) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                OrderList_User orderListScreen = new OrderList_User(userID);
                orderListScreen.setVisible(true);
            }
        });
    }
}
