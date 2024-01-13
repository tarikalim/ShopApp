import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteProduct extends JFrame {
    private JTextField productIDField;
    private JButton deleteButton;

    public DeleteProduct() {
        setTitle("Delete Product Screen");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        productIDField = new JTextField(10);
        deleteButton = new JButton("Delete product");

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Enter productID to delete:"));
        panel.add(productIDField);
        panel.add(new JLabel());
        panel.add(deleteButton);

        add(panel);

        setLocationRelativeTo(null);

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productIDString = productIDField.getText();
                try {
                    int productID = Integer.parseInt(productIDString);
                    deleteProduct(productID);
                    JOptionPane.showMessageDialog(null, "Product deleted.");
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid productID.");
                }
            }
        });
    }

    private void deleteProduct(int productID) {
        Connection connection = null;
        PreparedStatement deleteFromCartDetailsStmt = null;
        PreparedStatement deleteFromOrderDetailsStmt = null;
        PreparedStatement deleteFromReviewsStmt = null;
        PreparedStatement deleteFromProductsStmt = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);


            String deleteFromCartDetailsQuery = "DELETE FROM CartDetails WHERE ProductID = ?";
            deleteFromCartDetailsStmt = connection.prepareStatement(deleteFromCartDetailsQuery);
            deleteFromCartDetailsStmt.setInt(1, productID);
            deleteFromCartDetailsStmt.executeUpdate();


            String deleteFromOrderDetailsQuery = "DELETE FROM OrderDetails WHERE ProductID = ?";
            deleteFromOrderDetailsStmt = connection.prepareStatement(deleteFromOrderDetailsQuery);
            deleteFromOrderDetailsStmt.setInt(1, productID);
            deleteFromOrderDetailsStmt.executeUpdate();


            String deleteFromReviewsQuery = "DELETE FROM Review WHERE ProductID = ?";
            deleteFromReviewsStmt = connection.prepareStatement(deleteFromReviewsQuery);
            deleteFromReviewsStmt.setInt(1, productID);
            deleteFromReviewsStmt.executeUpdate();


            String deleteFromProductsQuery = "DELETE FROM Products WHERE ProductID = ?";
            deleteFromProductsStmt = connection.prepareStatement(deleteFromProductsQuery);
            deleteFromProductsStmt.setInt(1, productID);
            deleteFromProductsStmt.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error, product didn't delete.");
        } finally {

            try {
                if (deleteFromCartDetailsStmt != null) deleteFromCartDetailsStmt.close();
                if (deleteFromOrderDetailsStmt != null) deleteFromOrderDetailsStmt.close();
                if (deleteFromReviewsStmt != null) deleteFromReviewsStmt.close();
                if (deleteFromProductsStmt != null) deleteFromProductsStmt.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
