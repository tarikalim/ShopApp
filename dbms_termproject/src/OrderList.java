import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OrderList extends JFrame {
    private JTable orderListTable;
    private JButton refreshButton, viewDetailsButton, cancelOrderButton, showDailyReportButton, changeOrderStatusButton;

    public OrderList() {
        setTitle("Order List");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        orderListTable = new JTable();
        orderListTable.setFont(new Font("Arial", Font.PLAIN, 16));
        orderListTable.setRowHeight(30);

        refreshButton = new JButton("Refresh");
        viewDetailsButton = new JButton("View Details");
        cancelOrderButton = new JButton("Cancel Order");
        showDailyReportButton = new JButton("Show Daily Report");
        changeOrderStatusButton = new JButton("Change Order Status");

        // Font ve Renk Ayarları
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color backgroundColor = new Color(230, 240, 250);
        Color buttonColor = new Color(0, 0, 128);

        setButtonStyle(refreshButton, buttonColor, buttonFont);
        setButtonStyle(viewDetailsButton, buttonColor, buttonFont);
        setButtonStyle(cancelOrderButton, buttonColor, buttonFont);
        setButtonStyle(showDailyReportButton, buttonColor, buttonFont);
        setButtonStyle(changeOrderStatusButton, buttonColor, buttonFont);

        refreshButton.addActionListener(e -> refreshOrderList());
        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        cancelOrderButton.addActionListener(e -> cancelOrder());
        showDailyReportButton.addActionListener(e -> showDailyReport());
        changeOrderStatusButton.addActionListener(e -> changeOrderStatus());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(cancelOrderButton);
        buttonPanel.add(showDailyReportButton);
        buttonPanel.add(changeOrderStatusButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(orderListTable), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        refreshOrderList();
    }

    private void refreshOrderList() {
        // Sipariş listesini veritabanından çekip tabloya ekle
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("OrderID");
        model.addColumn("UserID");
        model.addColumn("Address");
        model.addColumn("OrderDate");
        model.addColumn("TotalAmount");
        model.addColumn("Status");
        model.addColumn("PaymentType");


        try {
            Connection connection = DatabaseConnection.getConnection();

            String query = "SELECT Orders.OrderID, Orders.UserID, Orders.Address, Orders.OrderDate, SUM(OrderDetails.SalePrice) AS TotalAmount, Orders.Status, Orders.PaymentType " +
                    "FROM Orders " +
                    "INNER JOIN OrderDetails ON Orders.OrderID = OrderDetails.OrderID " +
                    "INNER JOIN Users ON Orders.UserID = Users.UserID " +
                    "GROUP BY Orders.OrderID";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int orderID = resultSet.getInt("OrderID");
                int userID = resultSet.getInt("UserID");
                String address = resultSet.getString("Address");
                String orderDate = resultSet.getString("OrderDate");
                double totalAmount = resultSet.getDouble("TotalAmount");
                String status = resultSet.getString("Status");
                String paymentType = resultSet.getString("PaymentType");


                model.addRow(new Object[]{orderID, userID, address, orderDate, totalAmount, status, paymentType});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        orderListTable.setModel(model);
    }

    private void viewOrderDetails() {
        // Belirtilen siparişin detaylarını gösteren bir ekranı aç
        String orderIDString = JOptionPane.showInputDialog("For details please enter OrderID:");
        try {
            int orderID = Integer.parseInt(orderIDString);
            SwingUtilities.invokeLater(() -> {
                OrderDetail orderDetails = new OrderDetail(orderID);
                orderDetails.setVisible(true);
            });
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelOrder() {
        // Siparişi iptal et
        String orderIDString = JOptionPane.showInputDialog("To cancel the order, enter the order ID:");
        try {
            int orderID = Integer.parseInt(orderIDString);
            Connection connection = DatabaseConnection.getConnection();

            // Önce OrderDetails tablosundaki ilgili verileri sil
            String deleteOrderDetailsQuery = "DELETE FROM OrderDetails WHERE OrderID = ?";
            PreparedStatement deleteOrderDetailsStatement = connection.prepareStatement(deleteOrderDetailsQuery);
            deleteOrderDetailsStatement.setInt(1, orderID);
            deleteOrderDetailsStatement.executeUpdate();

            // Ardından Orders tablosundaki siparişi sil
            String deleteOrderQuery = "DELETE FROM Orders WHERE OrderID = ?";
            PreparedStatement deleteOrderStatement = connection.prepareStatement(deleteOrderQuery);
            deleteOrderStatement.setInt(1, orderID);
            int rowsAffected = deleteOrderStatement.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Sipariş iptal edildi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                // Sipariş iptal edildikten sonra sipariş listesini yenile
                refreshOrderList();
            } else {
                JOptionPane.showMessageDialog(null, "Sipariş iptal edilemedi.", "Hata", JOptionPane.ERROR_MESSAGE);
            }

            deleteOrderDetailsStatement.close();
            deleteOrderStatement.close();
            connection.close();
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDailyReport() {
        // DailyReport sınıfını çağırarak günlük raporu göster
        SwingUtilities.invokeLater(() -> {
            DailyReport dailyReport = new DailyReport();
            dailyReport.setVisible(true);
        });
    }

    private void changeOrderStatus() {
        // Order statusunu değiştirmek için işlem yap
        String orderIDString = JOptionPane.showInputDialog("Enter OrderID to change status:");
        if (orderIDString == null || orderIDString.isEmpty()) {
            // Eğer sipariş ID'si girilmezse veya boşsa, işlem yapmadan pencereyi kapat
            return;
        }

        try {
            int orderID = Integer.parseInt(orderIDString);

            // Siparişin mevcut durumunu al
            String[] statuses = {"Order Placed", "Shipped", "Delivered"};
            String selectedStatus = (String) JOptionPane.showInputDialog(
                    null,
                    "Select new status:",
                    "Change Order Status",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statuses,
                    statuses[0]
            );

            if (selectedStatus != null) {
                // Seçilen yeni durumu güncelle
                Connection connection = DatabaseConnection.getConnection();
                String updateStatusQuery = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
                PreparedStatement updateStatusStatement = connection.prepareStatement(updateStatusQuery);
                updateStatusStatement.setString(1, selectedStatus);
                updateStatusStatement.setInt(2, orderID);
                int rowsAffected = updateStatusStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Order status updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshOrderList();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update order status.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                updateStatusStatement.close();
                connection.close();
            }
        } catch (NumberFormatException | SQLException e) {
        }
    }

    private void setButtonStyle(JButton button, Color buttonColor, Font buttonFont) {
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(buttonFont);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

    public static void display() {
        SwingUtilities.invokeLater(() -> {
            OrderList orderList = new OrderList();
            orderList.setVisible(true);
        });
    }
}
