import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private static ImageIcon loadScaledIcon(String path, int width, int height) {
        ImageIcon originalIcon = new ImageIcon(path);
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    public static void display() {
        JFrame frame = new JFrame("Login Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(230, 240, 250));
        frame.add(panel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        ImageIcon userIcon = loadScaledIcon("C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\usernameicon.png", 20, 20); // 20x20 boyut
        ImageIcon passwordIcon = loadScaledIcon("C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\passwordicon.png", 20, 20); // 20x20 boyut


        JLabel userIconLabel = new JLabel(userIcon);
        JLabel passwordIconLabel = new JLabel(passwordIcon);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JTextField userText = new JTextField(15);
        JPasswordField passwordText = new JPasswordField(15);

        JButton userButton = new JButton("User Login");
        userButton.setBackground(new Color(218, 218, 218));
        JButton sellerButton = new JButton("Seller Login");
        sellerButton.setBackground(new Color(218, 218, 218));
        JButton signInButton = new JButton("Sign Up");
        signInButton.setBackground(new Color(218, 218, 218));


        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userIconLabel, gbc);

        gbc.gridx = 1;
        panel.add(userText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordIconLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(userButton, gbc);

        gbc.gridy = 3;
        panel.add(sellerButton, gbc);

        gbc.gridy = 4;
        panel.add(signInButton, gbc);


        userButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                int userID = validateLogin(username, password);
                if (userID != -1) {
                    frame.dispose();
                    UserScreen.display(userID);
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid username or password!");
                }
            }
        });

        sellerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                if ("admin".equals(username) && "admin123".equals(password)) {
                    frame.dispose();
                    SellerScreen.display();
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid username or password for seller!");
                }
            }
        });


        signInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                SignIn.display();
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static int validateLogin(String username, String password) {
        String sql = "SELECT UserID FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("UserID");
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
}
