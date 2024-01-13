import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DailyReport extends JFrame {
    private JTable dailyReportTable;
    private JLabel totalAmountLabel;

    public DailyReport() {
        setTitle("Daily Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dailyReportTable = new JTable();
        dailyReportTable.setFont(new Font("Arial", Font.PLAIN, 16));
        dailyReportTable.setRowHeight(30);

        totalAmountLabel = new JLabel();
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(dailyReportTable), BorderLayout.CENTER);
        panel.add(totalAmountLabel, BorderLayout.SOUTH);

        add(panel);

        setLocationRelativeTo(null);
        showDailyReport();
    }

    private void showDailyReport() {

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("OrderID");
        model.addColumn("UserID");
        model.addColumn("OrderDate");
        model.addColumn("TotalAmount");

        double totalDailyAmount = 0.0;

        try {
            Connection connection = DatabaseConnection.getConnection();


            java.util.Date today = new java.util.Date();
            java.sql.Date sqlToday = new java.sql.Date(today.getTime());

            String query = "SELECT Orders.OrderID, Orders.UserID, Orders.OrderDate, SUM(OrderDetails.SalePrice) AS TotalAmount " +
                    "FROM Orders " +
                    "INNER JOIN OrderDetails ON Orders.OrderID = OrderDetails.OrderID " +
                    "WHERE Orders.OrderDate = ? " +
                    "GROUP BY Orders.OrderID";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, sqlToday);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int orderID = resultSet.getInt("OrderID");
                int userID = resultSet.getInt("UserID");
                String orderDate = resultSet.getString("OrderDate");
                double orderTotalAmount = resultSet.getDouble("TotalAmount");

                totalDailyAmount += orderTotalAmount; // Günlük toplam fiyatı güncelle

                model.addRow(new Object[]{orderID, userID, orderDate, orderTotalAmount});
            }


            String totalAmountQuery = "SELECT SUM(TotalAmount) AS DailyTotal FROM (" + query + ") AS SubQuery";
            PreparedStatement totalAmountStatement = connection.prepareStatement(totalAmountQuery);
            totalAmountStatement.setDate(1, sqlToday);
            ResultSet totalAmountResultSet = totalAmountStatement.executeQuery();

            if (totalAmountResultSet.next()) {
                totalDailyAmount = totalAmountResultSet.getDouble("DailyTotal");
            }

            resultSet.close();
            preparedStatement.close();
            totalAmountResultSet.close();
            totalAmountStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dailyReportTable.setModel(model);

        totalAmountLabel.setText("Total Daily Amount: " + totalDailyAmount + " TL");
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
