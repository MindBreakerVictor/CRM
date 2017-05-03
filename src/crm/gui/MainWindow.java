package crm.gui;

import crm.database.CRMDatabase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainWindow {

    // Database
    private CRMDatabase database;

    // Main frame
    private JFrame frame;
    private JPanel mainPanel;

    private JTabbedPane tabbedPane;

    // Products tab
    private JTextField productName;
    private JTextField productPrice;
    private JTextField productStock;
    private JButton addProduct;
    private JTable productsTable;
    private static final Object[] productsTableColumnNames = { "ID", "Name", "Price", "Stock" };

    {
        frame = new JFrame("Customer Relationship Management");
    }

    public MainWindow(CRMDatabase database) {
        this.database = database;

        // Products tab
        initiateProductsTab();

        // Initiate main frame
        initiateFrame();
    }

    private void initiateFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - screenSize.width / 4, screenSize.height / 2 - screenSize.height / 4,
                screenSize.width / 2, screenSize.height / 2);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void initiateProductsTab() {
        updateProductsTable();

        addProduct.addActionListener(e -> {
            try {
                this.database.insertProduct(productName.getText(), Double.parseDouble(productPrice.getText()), Integer.parseInt(productStock.getText()));
                updateProductsTable();
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
    }

    private void updateProductsTable() {
        try {
            Object[][] data = database.getProducts();

            DefaultTableModel tableModel = data == null ? new DefaultTableModel(productsTableColumnNames, 0) :
                    new DefaultTableModel(data, productsTableColumnNames);

            productsTable.setModel(tableModel);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
