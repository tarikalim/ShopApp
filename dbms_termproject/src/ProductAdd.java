import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductAdd extends JFrame {
    private JTextField productNameField;
    private JTextField priceField;
    private JTextField stockQuantityField;
    private JTextArea descriptionField;
    private JComboBox<String> categoryComboBox;
    private JButton addButton;

    public ProductAdd() {
        setTitle("Add Product");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        productNameField = new JTextField(20);
        priceField = new JTextField(10);
        stockQuantityField = new JTextField(5);
        descriptionField = new JTextArea(5, 20);
        categoryComboBox = new JComboBox<>(getCategoryNames());
        categoryComboBox.setSelectedIndex(0);

        addButton = new JButton("Add Product");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String productName = productNameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stockQuantity = Integer.parseInt(stockQuantityField.getText());
                String description = descriptionField.getText();
                String selectedCategory = (String) categoryComboBox.getSelectedItem();

                int categoryID = getCategoryIDByName(selectedCategory);

                addProductToDatabase(productName, price, stockQuantity, description, categoryID);

                // Ekranı kapat
                dispose();
            }
        });

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Product Name:"));
        panel.add(productNameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockQuantityField);
        panel.add(new JLabel("Details:"));
        panel.add(new JScrollPane(descriptionField));
        panel.add(new JLabel("Category:"));
        panel.add(categoryComboBox);
        panel.add(new JLabel(""));
        panel.add(addButton);

        add(panel);


        setLocationRelativeTo(null);
    }


    private String[] getCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT CategoryName FROM Categories";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                categoryNames.add(resultSet.getString("CategoryName"));
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryNames.toArray(new String[0]);
    }


    private int getCategoryIDByName(String categoryName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT CategoryID FROM Categories WHERE CategoryName = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, categoryName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("CategoryID");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private void addProductToDatabase(String productName, double price, int stockQuantity, String description, int categoryID) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String insertQuery = "INSERT INTO Products (Name, Price, StockQuantity, Description, CategoryID) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, productName);
            preparedStatement.setDouble(2, price);
            preparedStatement.setInt(3, stockQuantity);
            preparedStatement.setString(4, description);
            preparedStatement.setInt(5, categoryID);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

            JOptionPane.showMessageDialog(this, "Product successfully added.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error");
        }
    }
}
