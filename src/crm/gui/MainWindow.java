package crm.gui;

import crm.data.*;
import crm.database.*;

import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.sql.SQLException;

/**
 * TODO:
 * 1. Add changeListener in individuals, companies and products tables.
 * 2. Validate data for fields in individuals, companies and products fields.
 */

public class MainWindow {

    // Database
    private CRMDatabase database;

    // Main frame
    private JFrame frame;
    private JPanel mainPanel;

    // 1. Accounting tab

    // a. Customers tab
    // i. Individuals tab
    private JTextField individualFirstName;
    private JTextField individualLastName;
    private JTextField individualDeliveryAddress;
    private JTextField individualContactNumber;
    private JButton addIndividual;
    private JTable individualsTable;
    public static final Object[] individualsTableColumnNames = { "ID", "First Name", "Last Name",
            "Delivery Address", "Contact Number" };
    // ii. Companies tab
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

    // b. Invoices tab

    // 2. Deposit tab

    // a. Products tab
    private JTextField productName;
    private JTextField productPrice;
    private JTextField productStock;
    private JButton addProduct;
    private JTable productsTable;
    public static final Object[] productsTableColumnNames = { "ID", "Name", "Price", "Stock" };

    // b. Check Deposit tab

    // c. Clear Deposit tab

    // 3. Invoice tab
    private JComboBox<Customer> customersDropDownList;
    private JComboBox<Object> productsDropDownList;
    private JTextField quantity;
    private JLabel totalPriceLabel;
    private JButton addProductButton;
    private JButton createInvoiceButton;
    private JTable invoiceProductsTable;
    private JLabel availability;
    private JLabel isInStockLabel;
    private JLabel disponibleQuantityLabel;
    private JButton checkByUIDButton;
    private JButton showDisplayButton;
    public static final Object[] invoiceProductsTableColumnsNames = {"ID", "Name", "Price", "Quantity"};
    private List<Product> invoiceProducts = new ArrayList<>();

    {
        frame = new JFrame("Customer Relationship Management");
        invoiceProducts = new ArrayList<>();
    }

    public MainWindow(CRMDatabase database) throws SQLException, CRMDBNotConnectedException {
        this.database = database;

        initiateAccountingTab();
        initiateDepositTab();
        initiateInvoiceTab();
        initiateMainFrame();
    }

    /**
     * Initiate main window.
     */
    private void initiateMainFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - screenSize.width / 4, screenSize.height / 2 - screenSize.height / 4,
                screenSize.width / 2, screenSize.height / 2);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Initiate Accounting tab.
     */
    private void initiateAccountingTab() {
        initiateCustomersTab();
        initiateInvoicesTab();
    }

    /**
     * Initiate Customers sub tab of Accounting tab.
     */
    private void initiateCustomersTab() {
        initiateIndividualsTab();
        initiateCompaniesTab();
    }

    /**
     * Initiate Individuals sub tab of Customer sub tab of Accounting tab.
     */
    private void initiateIndividualsTab() {
        updateIndividualsTable();

        addIndividual.addActionListener(e -> {
            try {
                database.insertCustomer(new Individual(individualFirstName.getText(), individualLastName.getText(),
                        individualDeliveryAddress.getText(), individualContactNumber.getText()));
                resetIndividualsTextFields();
                updateIndividualsTable();
                updateCustomersDropDownList();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            }
        });
    }

    /**
     * Update individuals table from Individuals sub tab of Customer sub tab of Accounting tab.
     */
    private void updateIndividualsTable() {
        try {
            Object[][] data = database.getIndividuals();
            DefaultTableModel tableModel = data == null ? new DefaultTableModel(individualsTableColumnNames, 0) :
                    new DefaultTableModel(data, individualsTableColumnNames);

            individualsTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Reset text fields in Individuals sub tab of Customer sub tab of Accounting tab.
     * This is used when inserting a new individual in the database to clear the text fields.
     */
    private void resetIndividualsTextFields() {
        individualFirstName.setText("");
        individualLastName.setText("");
        individualDeliveryAddress.setText("");
        individualContactNumber.setText("");
    }

    /**
     * Initiate Companies sub tab of Customer sub tab of Accounting tab.
     */
    private void initiateCompaniesTab() {
        updateCompaniesTable();

        addCompany.addActionListener(e -> {
            try {
                this.database.insertCustomer(new Company(companyName.getText(), companyFiscalCode.getText(),
                        companyBankAccount.getText(), companyHQAddress.getText(),
                        companyDeliveryAddress.getText(), companyContactNumber.getText()));
                resetCompaniesTextFields();
                updateCompaniesTable();
                updateCustomersDropDownList();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            }
        });
    }

    /**
     * Update companies table from Companies sub tab of Customer sub tab of Accounting tab.
     */
    private void updateCompaniesTable() {
        try {
            Object[][] data = database.getCompanies();
            DefaultTableModel tableModel = data == null ? new DefaultTableModel(companiesTableColumnNames, 0) :
                    new DefaultTableModel(data, companiesTableColumnNames);

            companiesTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Reset text fields in Companies sub tab of Customer sub tab of Accounting tab.
     * This is used when inserting a new company in the database to clear the text fields.
     */
    private void resetCompaniesTextFields() {
        companyName.setText("");
        companyFiscalCode.setText("");
        companyBankAccount.setText("");
        companyHQAddress.setText("");
        companyDeliveryAddress.setText("");
        companyContactNumber.setText("");
    }

    /**
     * Initiate the Invoices sub tab of Accounting tab.
     */
    private void initiateInvoicesTab() {
        // TODO
    }

    /**
     * Initiate the Deposit tab.
     */
    private void initiateDepositTab() {
        initiateProductsTab();
        initiateCheckDepositTab();
        initiateClearDepositTab();
    }

    /**
     * Initiate the Products sub tab of Deposit tab.
     */
    private void initiateProductsTab() {
        updateProductsTable();

        addProduct.addActionListener(e -> {
            try {
                this.database.insertProduct(productName.getText(), Double.parseDouble(productPrice.getText()),
                        Integer.parseInt(productStock.getText()));
                resetProductsTextFields();
                updateProductsTable();
                updateProductsDropDownList();
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            }
        });
    }

    /**
     * Update products table from Products sub tab of Deposit tab.
     */
    private void updateProductsTable() {
        try {
            Object[][] data = database.getProducts();
            DefaultTableModel tableModel = data == null ? new DefaultTableModel(productsTableColumnNames, 0) :
                    new DefaultTableModel(data, productsTableColumnNames);

            productsTable.setModel(tableModel);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Reset text fields in Products sub tab of Deposit tab.
     * This is used when inserting a new product in the database to clear the text fields.
     */
    private void resetProductsTextFields() {
        productName.setText("");
        productPrice.setText("");
        productStock.setText("");
    }

    /**
     * Initiate Check Deposit sub tab of Deposit tab.
     */
    private void initiateCheckDepositTab() {
        // TODO
    }

    /**
     * Initiate Clear Deposit sub tab of Deposit tab.
     */
    private void initiateClearDepositTab() {
        // TODO
    }

    /**
     * Initiate Invoice tab.
     */
    private void initiateInvoiceTab() {
        updateCustomersDropDownList();
        updateProductsDropDownList();

        quantity.getDocument().addDocumentListener(new DocumentListener() {
            private void update(DocumentEvent event) {
                if (quantity.getText().matches("\\d+") || quantity.getText().matches(""))
                    updateAvailability();
                else
                    availability.setText("Wrong input!");
            }

            public void changedUpdate(DocumentEvent event) { update(event); }

            public void removeUpdate(DocumentEvent event) { update(event); }

            public void insertUpdate(DocumentEvent event) { update(event); }
        });

        addProductButton.addActionListener(e -> {
            if (availability.getText().matches("Available")) {
                try {
                    Object[] o = database.getProductByName(productsDropDownList.getSelectedItem().toString());
                    Product product = new Product(Integer.parseInt(String.valueOf(o[0])),
                            o[1].toString(), Double.parseDouble(String.valueOf(o[2])),
                            Integer.parseInt(String.valueOf(quantity.getText())));

                    invoiceProducts.add(product);

                    int index = 0;
                    Object[][] ob = new Object[invoiceProducts.size()][4];

                    for (Product p : invoiceProducts) {
                        ob[index][0] = p.getId();
                        ob[index][1] = p.getName();
                        ob[index][2] = p.getPrice();
                        ob[index][3] = p.getQuantity();
                        ++index;
                    }

                    DefaultTableModel model = new DefaultTableModel(ob, invoiceProductsTableColumnsNames);
                    invoiceProductsTable.setModel(model);

                    Double price = Double.parseDouble(totalPriceLabel.getText()) +
                            (Double.parseDouble(String.valueOf(o[2])) * Double.parseDouble(quantity.getText()));
                    totalPriceLabel.setText(String.valueOf(price));
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidProductException exception) {
                    new ErrorWindow("Invalid product name passed to CRMDatabase::getProductByName at line " +
                            Integer.toString(exception.getStackTrace()[0].getLineNumber()));
                }
            }
        });

        createInvoiceButton.addActionListener(e -> {
            String customerDetails = customersDropDownList.getSelectedItem().toString();
            Integer customerId = Integer.parseInt(customerDetails.substring(0, customerDetails.indexOf(',')));

            try {
                Customer invoiceCustomer = database.getCustomerById(customerId);

                ArrayList<Object[]>  invoiceProducts = new ArrayList<>();

                for (int count = 0; count < invoiceProductsTable.getRowCount(); count++) {
                    Object[] o = new Object[4];
                    o[0] = invoiceProductsTable.getValueAt(count, 0);
                    o[1] = invoiceProductsTable.getValueAt(count, 1);
                    o[2] = invoiceProductsTable.getValueAt(count, 2);
                    o[3] = invoiceProductsTable.getValueAt(count, 3);
                    invoiceProducts.add(o);
                }

                //TODO Create an invoice in database

                System.out.println("Invoice created");
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            }
        });
    }

    /**
     * The fuck this does?
     */
    private void updateAvailability() {
        try {
            Object[] product = database.getProductByName((String) productsDropDownList.getSelectedItem());

            if (!quantity.getText().matches("")) {
                if (Integer.parseInt(quantity.getText()) > (Integer) product[3])
                    availability.setText("Unavailable");
                else
                    availability.setText("Available");
            }
            else
                availability.setText("Check availablity");
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        } catch (InvalidProductException exception) {
            new ErrorWindow("Invalid product name passed to CRMDatabase::getProductByName at line " +
                    Integer.toString(exception.getStackTrace()[0].getLineNumber()));
        }
    }

    /**
     * Update customer drop down list(combo box) from Invoice tab.
     */
    private void updateCustomersDropDownList() {
        try {
            List<Customer> customers = database.getCustomers();

            customersDropDownList.removeAllItems();

            for (Customer c : customers)
                customersDropDownList.addItem(c);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Update products drop down list(combo box) from Invoice tab.
     */
    private void updateProductsDropDownList() {
        try {
            Object[][] products = database.getProducts();

            productsDropDownList.removeAllItems();

            if (products != null)
                for (Object[] p : products)
                    productsDropDownList.addItem(p[1]);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }
}
