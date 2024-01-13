import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OrderDetail extends JFrame {
    private JTable orderDetailTable;
    private JButton closeButton;

    public OrderDetail(int orderID) {
        setTitle("Order Details");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        orderDetailTable = new JTable();
        orderDetailTable.setFont(new Font("Arial", Font.PLAIN, 16));
        orderDetailTable.setRowHeight(30);

        closeButton = new JButton("Close");

        // Font ve Renk Ayarları
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color buttonColor = new Color(0, 0, 128);

        setButtonStyle(closeButton, buttonColor, buttonFont);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel panel = new JPanel();
        panel.add(closeButton);

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

        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT OD.ProductID, P.Name AS ProductName, OD.Quantity, OD.SalePrice " +
                    "FROM OrderDetails OD " +
                    "INNER JOIN Products P ON OD.ProductID = P.ProductID " +
                    "WHERE OD.OrderID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, orderID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int productID = resultSet.getInt("ProductID");
                String productName = resultSet.getString("ProductName");
                int quantity = resultSet.getInt("Quantity");
                double salePrice = resultSet.getDouble("SalePrice");

                model.addRow(new Object[]{productID, productName, quantity, salePrice});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        orderDetailTable.setModel(model);
    }

    private void setButtonStyle(JButton button, Color buttonColor, Font buttonFont) {
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(buttonFont);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

    public void display() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }
}
