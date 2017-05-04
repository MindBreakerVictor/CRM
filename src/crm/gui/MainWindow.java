package crm.gui;

import crm.data.Company;
import crm.data.Individual;
import crm.database.CRMDBNotConnectedException;
import crm.database.CRMDatabase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class MainWindow {

    // Database
    private CRMDatabase database;

    // Main frame
    private JFrame frame;
    private JPanel mainPanel;

    // Products tab
    private JTextField productName;
    private JTextField productPrice;
    private JTextField productStock;
    private JButton addProduct;
    private JTable productsTable;
    public static final Object[] productsTableColumnNames = { "ID", "Name", "Price", "Stock" };

    // Customers tab
    // Individual sub tab
    private JTextField individualFirstName;
    private JTextField individualLastName;
    private JTextField individualDeliveryAddress;
    private JTextField individualContactNumber;
    private JButton addIndividual;
    private JTable individualsTable;
    public static final Object[] individualsTableColumnNames = { "ID", "First Name", "Last Name",
            "Delivery Address", "Contact Number" };
    // Companies sub tab
    private JTable companiesTable;
    private JTextField companyName;
    private JTextField companyFiscalCode;
    private JTextField companyBankAccount;
    private JTextField companyHQAddress;
    private JTextField companyDeliveryAddress;
    private JTextField companyContactNumber;
    private JButton addCompany;
    public static final Object[] companiesTableColumnNames = { "ID", "Name", "Fiscal Code", "Bank Account",
            "Headquarters Address", "Delivery Address", "Contact Number" };

    // Invoices tab

    {
        frame = new JFrame("Customer Relationship Management");
    }

    public MainWindow(CRMDatabase database) {
        this.database = database;

        initiateProductsTab();
        initiateCustomersTab();
        initiateMainFrame();
    }

    private void initiateMainFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - screenSize.width / 4, screenSize.height / 2 - screenSize.height / 4,
                screenSize.width / 2, screenSize.height / 2);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void initiateCustomersTab() {
        initiateIndividualsTab();
        initiateCompaniesTab();
    }

    private void initiateIndividualsTab() {
        updateIndividualsTable();

        addIndividual.addActionListener(e -> {
            try {
                this.database.insertCustomer(new Individual(individualFirstName.getText(), individualLastName.getText(),
                        individualDeliveryAddress.getText(), individualContactNumber.getText()));
                updateIndividualsTable();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
                System.out.println("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
                System.out.println("SQL error: " + exception.getMessage());
            }
        });
    }

    private void updateIndividualsTable() {
        try {
            Object[][] data = database.getIndividuals();

            DefaultTableModel tableModel = data == null ? new DefaultTableModel(individualsTableColumnNames, 0) :
                    new DefaultTableModel(data, individualsTableColumnNames);

            individualsTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
            System.out.println("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
            System.out.println("SQL error: " + exception.getMessage());
        }
    }

    private void initiateCompaniesTab() {
        updateCompaniesTable();

        addCompany.addActionListener(e -> {
            try {
                this.database.insertCustomer(new Company(companyName.getText(), companyFiscalCode.getText(),
                        companyBankAccount.getText(), companyHQAddress.getText(),
                        companyDeliveryAddress.getText(), companyContactNumber.getText()));
                updateCompaniesTable();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
                System.out.println("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
                System.out.println("SQL error: " + exception.getMessage());
            }
        });
    }

    private void updateCompaniesTable() {
        try {
            Object[][] data = database.getCompanies();

            DefaultTableModel tableModel = data == null ? new DefaultTableModel(companiesTableColumnNames, 0) :
                    new DefaultTableModel(data, companiesTableColumnNames);

            companiesTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
            System.out.println("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
            System.out.println("SQL error: " + exception.getMessage());
        }
    }

    private void initiateProductsTab() {
        updateProductsTable();

        addProduct.addActionListener(e -> {
            try {
                this.database.insertProduct(productName.getText(), Double.parseDouble(productPrice.getText()),
                        Integer.parseInt(productStock.getText()));
                updateProductsTable();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
                System.out.println("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
                System.out.println("SQL error: " + exception.getMessage());
            }
        });
    }

    private void updateProductsTable() {
        try {
            Object[][] data = database.getProducts();

            DefaultTableModel tableModel = data == null ? new DefaultTableModel(productsTableColumnNames, 0) :
                    new DefaultTableModel(data, productsTableColumnNames);

            productsTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
            System.out.println("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
            System.out.println("SQL error: " + exception.getMessage());
        }
    }
}
