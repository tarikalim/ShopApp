import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignIn extends JFrame {

    public SignIn() {
        setTitle("Sign Up");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(230, 240, 250)); // Yumuşak bir arka plan rengi
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("User Name:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JTextField userText = new JTextField(20);
        addComponent(panel, userLabel, 0, 0, constraints);
        addComponent(panel, userText, 1, 0, constraints);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JPasswordField passwordText = new JPasswordField(20);
        addComponent(panel, passwordLabel, 0, 1, constraints);
        addComponent(panel, passwordText, 1, 1, constraints);

        JLabel emailLabel = new JLabel("E-mail:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JTextField emailText = new JTextField(20);
        addComponent(panel, emailLabel, 0, 2, constraints);
        addComponent(panel, emailText, 1, 2, constraints);

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setBackground(new Color(218, 218, 218));

        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                String email = emailText.getText();
                registerUser(username, password, email);
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        panel.add(signUpButton, constraints);

        add(panel);
    }
    private void addComponent(JPanel panel, JComponent component, int x, int y, GridBagConstraints constraints) {
        constraints.gridx = x;
        constraints.gridy = y;
        panel.add(component, constraints);
    }
    private void registerUser(String username, String password, String email) {
        String sql = "INSERT INTO Users (Username, Password, Email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Successful!");
            this.dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error when sign up!.");
        }
    }
    public static void display() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SignIn().setVisible(true);
            }
        });
    }
}
