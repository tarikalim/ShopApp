import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class ProductList_User extends JFrame {
    private JTable productListTable;
    private JButton refreshButton, addToCartButton, viewCartButton, seeReviewsButton;
    private JComboBox<String> categoryComboBox;
    private JTextField productIdField;
    private int userID;

    public ProductList_User(int userID) {
        this.userID = userID;

        setTitle("Product List");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        productListTable = new JTable();
        productListTable.setFont(new Font("Arial", Font.PLAIN, 16));
        productListTable.setRowHeight(30);

        // Font ve Renk Ayarları
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color backgroundColor = new Color(230, 240, 250);
        Color buttonColor = new Color(0, 0, 128);

        refreshButton = new JButton("Refresh");
        setButtonStyle(refreshButton, buttonColor, buttonFont);

        viewCartButton = new JButton("See Cart");
        setButtonStyle(viewCartButton, buttonColor, buttonFont);

        addToCartButton = new JButton("Add Product to Cart");
        setButtonStyle(addToCartButton, buttonColor, buttonFont);

        seeReviewsButton = new JButton("See Reviews");
        setButtonStyle(seeReviewsButton, buttonColor, buttonFont);

        productIdField = new JTextField(10);
        categoryComboBox = new JComboBox<>(getCategoryNames());

        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.add(refreshButton);
        panel.add(categoryComboBox);
        panel.add(productIdField);
        panel.add(addToCartButton);
        panel.add(viewCartButton);
        panel.add(seeReviewsButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(productListTable), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        categoryComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshProductListByCategory(categoryComboBox.getSelectedItem().toString());
            }
        });

        refreshButton.addActionListener(e -> refreshProductList());

        addToCartButton.addActionListener(e -> {
            try {
                int productId = Integer.parseInt(productIdField.getText());
                addToCart(productId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter valid ProductID.");
            }
        });

        viewCartButton.addActionListener(e -> {
            Cart cartScreen = new Cart(userID);
            cartScreen.setVisible(true);
        });

        seeReviewsButton.addActionListener(e -> {
            String productIDString = JOptionPane.showInputDialog("Enter Product ID to see reviews:");
            try {
                int productID = Integer.parseInt(productIDString);
                Review.display(productID);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid Product ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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
    private void addToCart(int productID) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            int cartID = getOrCreateCart(connection, userID);


            String sql = "INSERT INTO CartDetails (CartID, ProductID, Quantity) VALUES (?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE Quantity = Quantity + 1";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, cartID);
                pstmt.setInt(2, productID);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product added to Cart.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error.");
        }
    }

    private int getOrCreateCart(Connection connection, int userID) throws SQLException {
        String checkCartSql = "SELECT CartID FROM Cart WHERE UserID = ?";
        try (PreparedStatement checkCartStmt = connection.prepareStatement(checkCartSql)) {
            checkCartStmt.setInt(1, userID);
            ResultSet resultSet = checkCartStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("CartID");
            }
        }

        String insertCartSql = "INSERT INTO Cart (UserID) VALUES (?)";
        try (PreparedStatement insertCartStmt = connection.prepareStatement(insertCartSql, Statement.RETURN_GENERATED_KEYS)) {
            insertCartStmt.setInt(1, userID);
            insertCartStmt.executeUpdate();
            ResultSet generatedKeys = insertCartStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Error.");
            }
        }
    }
    private void setButtonStyle(JButton button, Color buttonColor, Font buttonFont) {
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(buttonFont);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

}