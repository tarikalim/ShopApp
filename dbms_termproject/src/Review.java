import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Review extends JFrame {
    private JTable reviewTable;
    private JLabel averageRatingLabel;
    private int productID;

    public Review(int productID) {
        this.productID = productID;
        setTitle("Product Reviews");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        reviewTable = new JTable();
        averageRatingLabel = new JLabel();
        loadProductReviews(productID);
        loadAverageRating(productID);

        add(new JScrollPane(reviewTable), BorderLayout.CENTER);
        add(averageRatingLabel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private void loadProductReviews(int productID) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Username");
        model.addColumn("Comment");
        model.addColumn("Rating");
        model.addColumn("Date");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT Users.Username, Review.comment, Review.rate, Review.date " +
                    "FROM Review " +
                    "INNER JOIN Users ON Review.userID = Users.UserID " +
                    "WHERE Review.productID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, productID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                String comment = resultSet.getString("comment");
                int rating = resultSet.getInt("rate");
                String date = dateFormat.format(resultSet.getDate("date"));

                model.addRow(new Object[]{username, comment, rating, date});
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        reviewTable.setModel(model);
    }

    private void loadAverageRating(int productID) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT AVG(rate) AS AvgRating FROM Review WHERE productID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, productID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double averageRating = resultSet.getDouble("AvgRating");
                if (!resultSet.wasNull()) {
                    averageRatingLabel.setText("Average Rating: " + decimalFormat.format(averageRating));
                } else {
                    averageRatingLabel.setText("No reviews yet.");
                }
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void display(int productID) {
        SwingUtilities.invokeLater(() -> {
            Review frame = new Review(productID);
            frame.setVisible(true);
        });
    }
}
