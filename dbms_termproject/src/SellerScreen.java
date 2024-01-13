import javax.swing.*;
import java.awt.*;

public class SellerScreen extends JFrame {
    private JButton addButton;
    private JButton viewProductsButton;
    private JButton viewOrdersButton;

    public SellerScreen() {
        setTitle("Seller");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(240, 240, 240));

        addButton = createButton("Add Product", new Color(0, 0, 128), "C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\addProducticon.png");
        viewProductsButton = createButton("View Products", new Color(0, 0, 128), "C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\viewProducticon.png");
        viewOrdersButton = createButton("View Orders", new Color(0, 0, 128), "C:\\Users\\Tarik\\Desktop\\dbms_termproject\\src\\viewOrdersicon.png");


        addButton.addActionListener(e -> showProductAddScreen());
        viewProductsButton.addActionListener(e -> showProductListScreen());
        viewOrdersButton.addActionListener(e -> showOrderListScreen());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(viewProductsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(viewOrdersButton);

        add(buttonPanel, BorderLayout.WEST);
        setLocationRelativeTo(null);
    }

    private void showProductAddScreen() {
        ProductAdd productAddScreen = new ProductAdd();
        productAddScreen.setVisible(true);
    }

    private void showProductListScreen() {
        ProductList productListScreen = new ProductList();
        productListScreen.setVisible(true);
    }

    private void showOrderListScreen() {
        OrderList orderList = new OrderList();
        orderList.setVisible(true);
    }

    private JButton createButton(String text, Color color, String iconPath) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 100));

        // İkonu ekle
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(35, 35,  java.awt.Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        button.setIcon(icon);

        return button;
    }

    public static void display() {
        SwingUtilities.invokeLater(() -> new SellerScreen().setVisible(true));
    }
}
