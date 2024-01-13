import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class ProductList extends JFrame {
    private JTable productListTable;
    private JButton refreshButton, editButton, deleteProductButton;
    private JComboBox<String> categoryComboBox;

    public ProductList() {
        setTitle("Product List");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        productListTable = new JTable();
        productListTable.setFont(new Font("Arial", Font.PLAIN, 16));
        productListTable.setRowHeight(30);

        refreshButton = new JButton("Refresh");
        editButton = new JButton("Edit");
        deleteProductButton = new JButton("Delete Product");
        categoryComboBox = new JComboBox<>(getCategoryNames());

        // Font ve Renk Ayarları
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color backgroundColor = new Color(0, 0, 128);
        Color buttonColor = new Color(0, 0, 128);

        setButtonStyle(refreshButton, buttonColor, buttonFont);
        setButtonStyle(editButton, buttonColor, buttonFont);
        setButtonStyle(deleteProductButton, buttonColor, buttonFont);

        refreshButton.addActionListener(e -> refreshProductList());

        editButton.addActionListener(e -> {
            String productIDString = JOptionPane.showInputDialog("Enter ProductID for edit product:");
            try {
                int productID = Integer.parseInt(productIDString);
                openEditProductScreen(productID);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid ID.");
            }
        });

        deleteProductButton.addActionListener(e -> openDeleteProductScreen());

        categoryComboBox.addActionListener(e -> refreshProductListByCategory(categoryComboBox.getSelectedItem().toString()));

        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.add(refreshButton);
        panel.add(categoryComboBox);
        panel.add(editButton);
        panel.add(deleteProductButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(productListTable), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        refreshProductList();
    }

    private String[] getCategoryNames() {
        ArrayList<String> categoryNames = new ArrayList<>();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT CategoryName FROM Categories";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String categoryName = resultSet.getString("CategoryName");
                categoryNames.add(categoryName);
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryNames.toArray(new String[0]);
    }

    private void refreshProductList() {

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ProductID");
        model.addColumn("Product Name");
        model.addColumn("Price");
        model.addColumn("Stock");
        model.addColumn("Details");
        model.addColumn("Category");

        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT Products.ProductID, Products.Name, Products.Price, Products.StockQuantity, Products.Description, Categories.CategoryName " +
                    "FROM Products " +
                    "INNER JOIN Categories ON Products.CategoryID = Categories.CategoryID";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int productID = resultSet.getInt("ProductID");
                String productName = resultSet.getString("Name");
                double price = resultSet.getDouble("Price");
                int stockQuantity = resultSet.getInt("StockQuantity");
                String description = resultSet.getString("Description");
                String categoryName = resultSet.getString("CategoryName");

                model.addRow(new Object[]{productID, productName, price, stockQuantity, description, categoryName});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        productListTable.setModel(model);
    }

    private void refreshProductListByCategory(String categoryName) {
        // Seçilen kategoriye göre ürünleri listele
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ProductID"); // ProductID sütunu eklendi
        model.addColumn("Product Name");
        model.addColumn("Price");
        model.addColumn("Stock");
        model.addColumn("Details");
        model.addColumn("Category");

        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT Products.ProductID, Products.Name, Products.Price, Products.StockQuantity, Products.Description, Categories.CategoryName " +
                    "FROM Products " +
                    "INNER JOIN Categories ON Products.CategoryID = Categories.CategoryID " +
                    "WHERE Categories.CategoryName = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, categoryName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int productID = resultSet.getInt("ProductID");
                String productName = resultSet.getString("Name");
                double price = resultSet.getDouble("Price");
                int stockQuantity = resultSet.getInt("StockQuantity");
                String description = resultSet.getString("Description");
                String category = resultSet.getString("CategoryName");

                model.addRow(new Object[]{productID, productName, price, stockQuantity, description, category});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        productListTable.setModel(model);
    }

    private void openEditProductScreen(int productID) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditProduct editProductScreen = new EditProduct(productID);
                editProductScreen.setVisible(true);
            }
        });
    }

    private void openDeleteProductScreen() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DeleteProduct deleteProductScreen = new DeleteProduct();
                deleteProductScreen.setVisible(true);
            }
        });
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
            ProductList productList = new ProductList();
            productList.setVisible(true);
        });
    }
}