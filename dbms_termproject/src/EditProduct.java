import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EditProduct extends JFrame {
    private JTextField productNameField;
    private JTextField priceField;
    private JTextField stockQuantityField;
    private JTextArea descriptionTextArea;
    private JComboBox<String> categoryComboBox;
    private JButton saveButton;

    public EditProduct(int productID) {
        setTitle("Ürün Düzenleme Ekranı");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        JLabel productNameLabel = new JLabel("Product name:");
        productNameField = new JTextField();
        JLabel priceLabel = new JLabel("Price:");
        priceField = new JTextField();
        JLabel stockQuantityLabel = new JLabel("Stock:");
        stockQuantityField = new JTextField();
        JLabel descriptionLabel = new JLabel("Details:");
        descriptionTextArea = new JTextArea();
        JLabel categoryLabel = new JLabel("Category:");


        ArrayList<String> categoryNames = getCategories();
        categoryComboBox = new JComboBox<>(categoryNames.toArray(new String[0]));

        saveButton = new JButton("Save");

        panel.add(productNameLabel);
        panel.add(productNameField);
        panel.add(priceLabel);
        panel.add(priceField);
        panel.add(stockQuantityLabel);
        panel.add(stockQuantityField);
        panel.add(descriptionLabel);
        panel.add(new JScrollPane(descriptionTextArea));
        panel.add(categoryLabel);
        panel.add(categoryComboBox);
        panel.add(new JLabel());
        panel.add(saveButton);

        add(panel);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProduct(productID);
                JOptionPane.showMessageDialog(null, "Product info's updated.", "Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });


        setLocationRelativeTo(null);


        fillProductData(productID);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProduct(productID);
                dispose();
            }
        });
    }

    private ArrayList<String> getCategories() {
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
        return categoryNames;
    }

    private void fillProductData(int productID) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT Name, Price, StockQuantity, Description, CategoryID FROM Products WHERE ProductID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, productID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String productName = resultSet.getString("Name");
                double price = resultSet.getDouble("Price");
                int stockQuantity = resultSet.getInt("StockQuantity");
                String description = resultSet.getString("Description");
                int categoryID = resultSet.getInt("CategoryID");

                productNameField.setText(productName);
                priceField.setText(String.valueOf(price));
                stockQuantityField.setText(String.valueOf(stockQuantity));
                descriptionTextArea.setText(description);
                categoryComboBox.setSelectedIndex(categoryID - 1);
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProduct(int productID) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "UPDATE Products SET Name = ?, Price = ?, StockQuantity = ?, Description = ?, CategoryID = ? WHERE ProductID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, productNameField.getText());
            preparedStatement.setDouble(2, Double.parseDouble(priceField.getText()));
            preparedStatement.setInt(3, Integer.parseInt(stockQuantityField.getText()));
            preparedStatement.setString(4, descriptionTextArea.getText());


            String selectedCategory = categoryComboBox.getSelectedItem().toString();


            int categoryID = getCategoryID(selectedCategory);

            preparedStatement.setInt(5, categoryID);
            preparedStatement.setInt(6, productID);

            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCategoryID(String categoryName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT CategoryID FROM Categories WHERE CategoryName = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, categoryName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int categoryID = resultSet.getInt("CategoryID");
                return categoryID;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void display() {
        setVisible(true);
    }

}
