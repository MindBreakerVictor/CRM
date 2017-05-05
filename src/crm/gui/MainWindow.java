package crm.gui;

import crm.data.Company;
import crm.data.Customer;
import crm.data.Individual;
import crm.data.Product;
import crm.database.CRMDBNotConnectedException;
import crm.database.CRMDatabase;
import crm.database.InvalidProductException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.sql.SQLException;
import java.util.ArrayList;

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
    private JTabbedPane tabbedPane1;
    private JTabbedPane tabbedPane2;
    private JPanel checkProductPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JList list1;
    private JPanel invoicePanel;
    private JLabel labelCustomer;
    private JTextField quantity;
    private JLabel totalPriceLabel;
    private JComboBox comboCustomers;
    private JComboBox comboProducts;
    private JButton createInvoiceButton;
    private JButton addProductButton;
    private JTextField textField3;
    private JComboBox comboBox1;
    private JTextArea textArea1;
    private JButton listAllInvoicesButton;
    private JButton listCompaniesInvoicesButton;
    private JButton listIndividualsInvoicesButton;
    private JTable invoiceProductsTable;
    private JLabel availability;
    private JLabel isInStockLabel;
    private JLabel disponibleQuantityLabel;
    private JButton checkByUIDButton;
    private JButton showDisplayButton;
    public static final Object[] companiesTableColumnNames = { "ID", "Name", "Fiscal Code", "Bank Account",
            "Headquarters Address", "Delivery Address", "Contact Number" };
    public static final Object[] invoiceProductsTableColumnsNames = {"ID", "Name", "Price", "Quantity"};
    private ArrayList<Product> invoiceProducts = new ArrayList<Product>();

    // Invoices tab

    {
        frame = new JFrame("Customer Relationship Management");
    }

    public MainWindow(CRMDatabase database) throws SQLException, CRMDBNotConnectedException {
        this.database = database;

        initiateProductsTab();
        initiateCustomersTab();
        initiateMainFrame();
        populateCustomersComboBox();
        populateProductsComboBox();

        quantity.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                String regex = "\\d+";
                if(quantity.getText().matches(regex) || quantity.getText().matches("")) {
                    updateAvailability();
                } else {
                    availability.setText("Wrong input!");
                }
            }

            public void removeUpdate(DocumentEvent e) {
                String regex = "\\d+";
                if(quantity.getText().matches(regex) || quantity.getText().matches("")) {
                    updateAvailability();
                } else {
                    availability.setText("Wrong input!");
                }
            }

            public void insertUpdate(DocumentEvent e) {
                String regex = "\\d+";
                if(quantity.getText().matches(regex) || quantity.getText().matches("")) {
                    updateAvailability();
                } else {
                    availability.setText("Wrong input!");
                }
            }
        });

        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (availability.getText().matches("Available")) {
                    try {
                        Object[] o = database.getProductByName(comboProducts.getSelectedItem().toString());
                        Product product = new Product(Integer.parseInt(String.valueOf(o[0])), o[1].toString(), Double.parseDouble(String.valueOf(o[2])),
                                Integer.parseInt(String.valueOf(quantity.getText())));
                        invoiceProducts.add(product);

                        Object[][] ob  = new Object[invoiceProducts.size()][4];
                        int index = 0;

                        for (Product p: invoiceProducts) {
                            ob[index][0] = p.getId();
                            ob[index][1] = p.getName();
                            ob[index][2] = p.getPrice();
                            ob[index][3] = p.getQuantity();
                            index++;
                        }

                        DefaultTableModel model = ob == null ? new DefaultTableModel(invoiceProductsTableColumnsNames, 0) :
                                new DefaultTableModel(ob, invoiceProductsTableColumnsNames);
                        invoiceProductsTable.setModel(model);

                        Double price = Double.parseDouble(totalPriceLabel.getText()) + (Double.parseDouble(String.valueOf(o[2])) * Double.parseDouble(quantity.getText()));
                        totalPriceLabel.setText(String.valueOf(price));

                    } catch (CRMDBNotConnectedException exception) {
                        new ErrorWindow("SQLite3 database disconnected.");
                        System.out.println("SQLite3 database disconnected.");
                    } catch (SQLException exception) {
                        new ErrorWindow("SQL error: " + exception.getMessage());
                        System.out.println("SQL error: " + exception.getMessage());
                    } catch (InvalidProductException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        createInvoiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String customerDetails = comboCustomers.getSelectedItem().toString();
                Integer customerId = Integer.parseInt(customerDetails.substring(0, customerDetails.indexOf(',')));
                try {
                    Customer invoiceCustomer = database.getCustomerById(customerId);

                    ArrayList<Object[]>  invoiceProducts = new ArrayList<>();
                    for (int count = 0; count < invoiceProductsTable.getRowCount(); count++){
                        Object[] o = new Object[4];
                        o[0] = invoiceProductsTable.getValueAt(count, 0);
                        o[1] = invoiceProductsTable.getValueAt(count, 1);
                        o[2] = invoiceProductsTable.getValueAt(count, 2);
                        o[3] = invoiceProductsTable.getValueAt(count, 3);
                        invoiceProducts.add(o);
                    }

                    //TODO Create an invoice in database

                    System.out.println("Invoice created");

                } catch (CRMDBNotConnectedException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateAvailability() {
        try {
            Object[] product = database.getProductByName((String)comboProducts.getSelectedItem());
            if (quantity.getText().matches("")) {
                availability.setText("Check availablity");
            } else {
                if (Integer.parseInt(quantity.getText()) > (Integer) product[3]) {
                    availability.setText("Unavailable");
                } else {
                    availability.setText("Available");
                }
            }
        } catch (CRMDBNotConnectedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidProductException e) {
            e.printStackTrace();
        }
    }

        /*
        quantity.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent inputMethodEvent) {
                try {
                    Object[] product = database.getProductByName((String)comboProducts.getItemAt(0));
                    if (Integer.parseInt(quantity.getText()) > (Integer)product[3] ) {
                        availability.setText("Unavailable");
                    } else {
                        availability.setText("Available");
                    }
                } catch (CRMDBNotConnectedException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (InvalidProductException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void caretPositionChanged(InputMethodEvent inputMethodEvent) {

            }
        }); */

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

    private void populateCustomersComboBox() throws SQLException, CRMDBNotConnectedException {
        ArrayList<Customer> customers = (ArrayList<Customer>) database.getCustomers();
        comboCustomers.addItemListener(itemEvent -> {

        });
        for (Customer c: customers) {
            comboCustomers.addItem(c);
        }
    }

    private void populateProductsComboBox() throws SQLException, CRMDBNotConnectedException {
        Object[][] products = database.getProducts();
        comboCustomers.addItemListener(itemEvent -> {

        });
        for (Object[] p: products) {
            comboProducts.addItem(p[1]);
        }
    }
}
