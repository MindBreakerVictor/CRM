package crm.gui;

import crm.data.*;
import crm.database.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.sql.SQLException;

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

    // c. Reports tab
    private JLabel bestSellProd;
    private JTable reportsTable;
    private JLabel biggestCustomer;
    private JLabel totalCompanies;
    private JLabel totalIndividuals;
    public static final Object[] reportsTableColumnNames = { "Customer ID", "Name", "Invoices No.", "Total Payment" };

    // 2. Deposit tab

    // a. Products tab
    private JTextField productName;
    private JTextField productPrice;
    private JTextField productStock;
    private JButton addProduct;
    private JTable productsTable;
    public static final Object[] productsTableColumnNames = { "ID", "Name", "Price", "Stock" };

    // b. Check Deposit tab
    private JTextField searchProductUID;
    private JTextField searchProductName;
    private JButton searchProductButton;
    private JTable foundProductsTable;

    // 3. Invoice tab
    private JComboBox<Customer> customersDropDownList;
    private JComboBox<Object> productsDropDownList;
    private JTextField quantity;
    private JLabel totalPriceLabel;
    private JButton addProductButton;
    private JButton createInvoiceButton;
    private JTable invoiceProductsTable;
    private JButton clearInvoiceButton;
    private JButton searchInvoices;
    private JTextField searchByCustomerName;
    private JTextField searchByUidInvoice;
    private JTextField searchByDate;
    private JTable invoicesTable;
    private JComboBox invoicesDropDownList;
    private JButton listAllInvoices;
    private JButton listCompaniesInvoices;
    private JButton listIndividualsInvoices;
    public static final Object[] invoiceProductsTableColumnsNames = {"ID", "Name", "Price", "Quantity" };
    public static final Object[] invoiceDisplayTableColumnsNames = {"UID", "Id Product", "Price", "Quantity" };
    private List<Product> invoiceProducts;

    private static Object[][] invoices = null;

    {
        frame = new JFrame("Customer Relationship Management");
        invoiceProducts = new ArrayList<>();
    }

    public MainWindow(CRMDatabase database) {
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
        initiateReportsTab();
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
        individualsTable.getTableHeader().setReorderingAllowed(false);

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

        individualsTable.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                TableModel tableModel = (TableModel)e.getSource();
                int id = (Integer)tableModel.getValueAt(row, 0);
                String firstName = (String)tableModel.getValueAt(row, 1);
                String lastName = (String)tableModel.getValueAt(row, 2);
                String deliveryAddress = (String)tableModel.getValueAt(row, 3);
                String contactNumber = (String)tableModel.getValueAt(row, 4);
                Individual individual = new Individual(firstName, lastName, id, deliveryAddress, contactNumber);

                try {
                    database.updateCustomer(individual);
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidCustomerException exception) {
                    new ErrorWindow("Invalid customer id passed to CRMDatabase::updateCustomer at line " +
                            Integer.toString(exception.getStackTrace()[0].getLineNumber()));
                }
            }
        });
    }

    private void initiateReportsTab() {
        updateReportsTable();

        try {
            Object[] bestSellProduct = database.getBestSellingProduct();

            if (bestSellProduct == null)
                bestSellProd.setText("No data available.");
            else
                bestSellProd.setText(bestSellProduct[1].toString());

            String bgCustomer = database.getBestCustomer();

            if (bgCustomer == null)
                biggestCustomer.setText("No data available.");
            else
                biggestCustomer.setText(bgCustomer);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        } catch (InvalidProductException exception) {
            new ErrorWindow("CRMDatabase.getProduct received an invalid id as parameter in CRMDatabase.getBestSellingProduct.");
        }
    }

    private void updateReportsTable() {
        try {
            Object[][] data = database.getCustomersPayments();

            DefaultTableModel tableModel = data == null ? new DefaultTableModel(reportsTableColumnNames, 0) :
                    new DefaultTableModel(data, reportsTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return false; }
                    };

            reportsTable.setModel(tableModel);
            updateTotalEarnings(data);
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    private void updateTotalEarnings(Object[][] data) {
        Double totalEarnedFromCompanies = 0.0;
        Double totalEarnedFromIndividuals = 0.0;

        totalIndividuals.setText(Double.toString(totalEarnedFromIndividuals));
        totalCompanies.setText(Double.toString(totalEarnedFromCompanies));

        if (data == null)
            return;

        try {
            for (Object[] rowData : data)
                if (database.isCompany((Integer) rowData[0]))
                    totalEarnedFromCompanies += (Double) rowData[3];
                else if (database.isIndividual((Integer) rowData[0]))
                    totalEarnedFromIndividuals += (Double) rowData[3];

            totalIndividuals.setText(Double.toString(totalEarnedFromIndividuals));
            totalCompanies.setText(Double.toString(totalEarnedFromCompanies));
        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Update individuals table from Individuals sub tab of Customer sub tab of Accounting tab.
     */
    private void updateIndividualsTable() {
        try {
            Object[][] data = database.getIndividuals();
            DefaultTableModel tableModel = data == null ? new DefaultTableModel(individualsTableColumnNames, 0) :
                    new DefaultTableModel(data, individualsTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return column != 0; }
                    };

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
        companiesTable.getTableHeader().setReorderingAllowed(false);

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

        companiesTable.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                TableModel tableModel = (TableModel)e.getSource();
                int id = (Integer)tableModel.getValueAt(row, 0);
                String name = (String)tableModel.getValueAt(row, 1);
                String fiscalCode = (String)tableModel.getValueAt(row, 2);
                String bankAccount = (String)tableModel.getValueAt(row, 3);
                String hqAddress = (String)tableModel.getValueAt(row, 4);
                String deliveryAddress = (String)tableModel.getValueAt(row, 5);
                String contactNumber = (String)tableModel.getValueAt(row, 6);
                Company company = new Company(name, fiscalCode, bankAccount, hqAddress, id, deliveryAddress, contactNumber);

                try {
                    database.updateCustomer(company);
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidCustomerException exception) {
                    new ErrorWindow("Invalid customer id passed to CRMDatabase::updateCustomer at line " +
                            Integer.toString(exception.getStackTrace()[0].getLineNumber()));
                }
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
                    new DefaultTableModel(data, companiesTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return column != 0; }
                    };

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
        updateInvoiceDropDownList(false);

        listAllInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                updateInvoiceDropDownList(false);
            }
        });

        listCompaniesInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {

                    invoices = database.getCompaniesInvoices();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (CRMDBNotConnectedException e) {
                    e.printStackTrace();
                }
                updateInvoiceDropDownList(true);
            }
        });


        listIndividualsInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {

                    invoices = database.getIndividualsInvoices();

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (CRMDBNotConnectedException e) {
                    e.printStackTrace();
                }
                updateInvoiceDropDownList(true);
            }
        });

        invoicesDropDownList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                updateInvoiceTable();
            }
        });

        searchInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchByUidInvoice.getText().equals("") && searchByCustomerName.getText().equals("") && searchByDate.getText().equals("")) {
                    updateInvoiceDropDownList(false);
                } else {
                    if (!searchByUidInvoice.getText().equals("")) {
                        try {

                            Object[] result = database.getInvoiceByUid(Integer.parseInt(searchByUidInvoice.getText()));
                            if (result != null) {
                                invoices = new Object[1][3];
                                invoices[0] = result;
                            } else
                                invoices = null;

                        } catch (CRMDBNotConnectedException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!searchByCustomerName.getText().equals("")) {
                            try {

                                Object[][] result = database.getInvoicesByCustomerName(searchByCustomerName.getText());
                                invoices = result;

                            } catch (CRMDBNotConnectedException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (!searchByDate.getText().equals("")) {
                                try {

                                    Object[][] result = database.getInvoicesByDate(searchByDate.getText());
                                    invoices = result;

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (CRMDBNotConnectedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    updateInvoiceDropDownList(true);
                    updateInvoiceTable();
                }
            }
        });
    }

    /**
     * Update the Invoice Table after search
     */
    private void updateInvoiceTable() {
        invoicesTable.getTableHeader().setReorderingAllowed(false);

        try {

            Object toListInvoice = invoicesDropDownList.getSelectedItem();
            if (toListInvoice != null) {

                String toListInvoiceUid = toListInvoice.toString().substring(3, toListInvoice.toString().indexOf("Date") - 2);
                Object[][] data = database.getProductsOfInvoice(Integer.parseInt(toListInvoiceUid));

                DefaultTableModel tableModel = data == null ? new DefaultTableModel(invoiceDisplayTableColumnsNames, 0) :
                        new DefaultTableModel(data, invoiceDisplayTableColumnsNames) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                invoicesTable.setModel(tableModel);
            } else {
                invoicesTable.removeAll();
            }

        } catch (CRMDBNotConnectedException exception) {
            new ErrorWindow("SQLite3 database disconnected.");
        } catch (SQLException exception) {
            new ErrorWindow("SQL error: " + exception.getMessage());
        }
    }

    /**
     * Initiate the Deposit tab.
     */
    private void initiateDepositTab() {
        initiateProductsTab();
        initiateCheckDepositTab();
    }

    /**
     * Initiate the Products sub tab of Deposit tab.
     */
    private void initiateProductsTab() {
        updateProductsTable();
        productsTable.getTableHeader().setReorderingAllowed(false);

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

        productsTable.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                TableModel tableModel = (TableModel)e.getSource();

                switch (e.getColumn()) {
                    case 2: // Price
                        try {
                            database.updateProductPrice((Integer)tableModel.getValueAt(e.getFirstRow(), 0),
                                    Double.parseDouble((String)tableModel.getValueAt(e.getFirstRow(), 2)));
                        } catch (CRMDBNotConnectedException exception) {
                            new ErrorWindow("SQLite3 database disconnected.");
                        } catch (SQLException exception) {
                            new ErrorWindow("SQL error: " + exception.getMessage());
                        } catch (InvalidProductException exception) {
                            new ErrorWindow("Invalid product id passed to CRMDatabase::updateProductPrice at line " +
                                    Integer.toString(exception.getStackTrace()[0].getLineNumber()));
                        }
                        break;
                    case 3: // Stock
                        try {
                            database.updateProductStock((Integer)tableModel.getValueAt(e.getFirstRow(), 0),
                                    Integer.parseInt((String)tableModel.getValueAt(e.getFirstRow(), 3)));
                        } catch (CRMDBNotConnectedException exception) {
                            new ErrorWindow("SQLite3 database disconnected.");
                        } catch (SQLException exception) {
                            new ErrorWindow("SQL error: " + exception.getMessage());
                        } catch (InvalidProductException exception) {
                            new ErrorWindow("Invalid product id passed to CRMDatabase::updateProductStock at line " +
                                    Integer.toString(exception.getStackTrace()[0].getLineNumber()));
                        }
                        break;
                    default:
                        break;
                }
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
                    new DefaultTableModel(data, productsTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return column != 0 && column != 1; }
                    };

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
        foundProductsTable.getTableHeader().setReorderingAllowed(false);
        foundProductsTable.setModel(new DefaultTableModel(productsTableColumnNames, 0));

        searchProductButton.addActionListener(e -> {
            if (!searchProductUID.getText().isEmpty()) {
                try {
                    Object[][] product = new Object[1][productsTableColumnNames.length];
                    product[0] = database.getProduct(Integer.parseInt(searchProductUID.getText()));

                    foundProductsTable.setModel(new DefaultTableModel(product, productsTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return false; }
                    });
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidProductException exception) {
                    new ErrorWindow("No product found!");
                }
            } else if (!searchProductName.getText().isEmpty()) {
                try {
                    Object[][] products = database.getProduct(searchProductName.getText());

                    foundProductsTable.setModel(new DefaultTableModel(products, productsTableColumnNames) {
                        @Override public boolean isCellEditable(int row, int column) { return false; }
                    });
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidProductException exception) {
                    new ErrorWindow("No products found!");
                }
            }
        });
    }

    /**
     * Initiate Invoice tab.
     */
    private void initiateInvoiceTab() {
        updateCustomersDropDownList();
        resetInvoice();
        updateAvailability();

        invoiceProductsTable.getTableHeader().setReorderingAllowed(false);

        productsDropDownList.addActionListener(e -> {
            if (productsDropDownList.getItemCount() > 0)
                quantity.setText("1");
        });

        quantity.getDocument().addDocumentListener(new DocumentListener() {
            private void update(DocumentEvent event) {
                if (quantity.getText().matches("^[1-9][0-9]*$") || quantity.getText().matches(""))
                    updateAvailability();
                else {
                    addProductButton.setText("Wrong input!");
                    addProductButton.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent event) { update(event); }

            public void removeUpdate(DocumentEvent event) { update(event); }

            public void insertUpdate(DocumentEvent event) { update(event); }
        });

        addProductButton.addActionListener(e -> {
            try {
                Object[] productData = database.getProductByName(productsDropDownList.getSelectedItem().toString());
                int productQuantity = Integer.parseInt(quantity.getText());

                invoiceProducts.add(new Product((Integer)productData[0], (String)productData[1],
                        (Double)productData[2], productQuantity));
                productsDropDownList.removeItemAt(productsDropDownList.getSelectedIndex());

                Object[][] ob = new Object[invoiceProducts.size()][4];

                for (int i = 0; i < invoiceProducts.size(); ++i) {
                    ob[i][0] = invoiceProducts.get(i).getId();
                    ob[i][1] = invoiceProducts.get(i).getName();
                    ob[i][2] = invoiceProducts.get(i).getPrice();
                    ob[i][3] = invoiceProducts.get(i).getQuantity();
                }

                invoiceProductsTable.setModel(new DefaultTableModel(ob, invoiceProductsTableColumnsNames) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                });

                Double price = Double.parseDouble(totalPriceLabel.getText()) + (Double)productData[2] * productQuantity;
                totalPriceLabel.setText(price.toString());
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            } catch (InvalidProductException exception) {
                new ErrorWindow("Invalid product name passed to CRMDatabase::getProductByName at line " +
                        Integer.toString(exception.getStackTrace()[0].getLineNumber()));
            }
        });

        createInvoiceButton.addActionListener(e -> {
            if (invoiceProductsTable.getRowCount() <= 0)
                return;

            String customerDetails = customersDropDownList.getSelectedItem().toString();
            Integer customerId = Integer.parseInt(customerDetails.substring(0, customerDetails.indexOf(',')));

            Object[][] products = new Object[invoiceProductsTable.getRowCount()][4];

            for (int i = 0; i < invoiceProductsTable.getRowCount(); ++i) {
                products[i][0] = invoiceProductsTable.getValueAt(i, 0);
                products[i][1] = invoiceProductsTable.getValueAt(i, 1);
                products[i][2] = invoiceProductsTable.getValueAt(i, 2);
                products[i][3] = invoiceProductsTable.getValueAt(i, 3);
            }

            try {
                database.insertInvoice(customerId, products);
            } catch (CRMDBNotConnectedException exception) {
                new ErrorWindow("SQLite3 database disconnected.");
            } catch (SQLException exception) {
                new ErrorWindow("SQL error: " + exception.getMessage());
            } catch (InvalidCustomerException exception) {
                new ErrorWindow("Attempted to insert an invoice with an invalid customer.");
            } catch (EmptyInvoiceException exception) {
                new ErrorWindow("Attempted to insert an invoice with no products.");
            } catch (InvalidProductException exception) {
                new ErrorWindow("Attempted to insert an invoice with an invalid product.");
            }

            // Update products stock
            for (Object[] product : products) {
                try {
                    int productId = (Integer) product[0];
                    database.updateProductStock(productId,
                            database.getProductStock(productId) - (Integer) product[3]);
                } catch (CRMDBNotConnectedException exception) {
                    new ErrorWindow("SQLite3 database disconnected.");
                } catch (SQLException exception) {
                    new ErrorWindow("SQL error: " + exception.getMessage());
                } catch (InvalidProductException exception) {
                    new ErrorWindow("Attempted to update the stock of an invalid product.");
                }
            }

            updateProductsTable();  // Update Deposit tab->Products sub-tab table also.
            updateReportsTable();
            resetInvoice();
        });

        clearInvoiceButton.addActionListener(e -> resetInvoice());
    }

    private void resetInvoice() {
        invoiceProducts.clear();
        invoiceProductsTable.setModel(new DefaultTableModel(invoiceProductsTableColumnsNames, 0));
        totalPriceLabel.setText("0.0");
        updateProductsDropDownList();
    }

    /**
     * Updates addProductButton
     */
    private void updateAvailability() {
        try {
            Object[] product = database.getProductByName((String)productsDropDownList.getSelectedItem());

            if (product == null || quantity.getText().equals("")) {
                addProductButton.setText("Check availability.");
                addProductButton.setEnabled(false);
                return;
            }

            if (Integer.parseInt(quantity.getText()) > (Integer)product[3]) {
                addProductButton.setText("Unavailable");
                addProductButton.setEnabled(false);
            } else {
                addProductButton.setText("Add Product");
                addProductButton.setEnabled(true);
            }
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

    /**
     * Update invoices drop down list(combo box) from Invoices tab.
     */
    private void updateInvoiceDropDownList(boolean filtering) {
        invoicesDropDownList.removeAllItems();

        if (!filtering) {
            try {
                invoices = database.getInvoices();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (CRMDBNotConnectedException e) {
                e.printStackTrace();
            }
        }
        if (invoices != null) {
            for (Object[] i : invoices) {
                invoicesDropDownList.addItem("Id:" + i[0] + "  Date:" + i[2].toString().substring(0, 10));
            }
        }
    }
}
