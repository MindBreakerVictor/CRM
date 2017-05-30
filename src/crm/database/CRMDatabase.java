package crm.database;

import crm.data.*;
import crm.gui.MainWindow;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import java.sql.*;

public class CRMDatabase implements AutoCloseable {

    private Connection connection;

    private static final String DatabaseCreation = "PRAGMA foreign_keys=ON;\n" +
        "CREATE TABLE IF NOT EXISTS `customer` (" +
            "`id` INTEGER NOT NULL, " +
            "`delivery_address` TEXT NOT NULL, " +
            "`contact_number` TEXT NOT NULL, " +
            "PRIMARY KEY(`id`), " +
            "UNIQUE(`contact_number`)" +
        ");\n" +
        "CREATE TABLE IF NOT EXISTS `individual` (" +
            "`customer_id` INTEGER NOT NULL, " +
            "`first_name` TEXT NOT NULL, " +
            "`last_name` TEXT NOT NULL, " +
            "PRIMARY KEY(`customer_id`), " +
            "FOREIGN KEY(`customer_id`) REFERENCES `customer`(`id`)" +
        ");\n" +
        "CREATE TABLE IF NOT EXISTS `company` (" +
            "`customer_id` INTEGER NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`fiscal_code` TEXT NOT NULL, " +
            "`bank_account` TEXT NOT NULL, " +
            "`hq_address` TEXT NOT NULL, " +
            "PRIMARY KEY(`customer_id`), " +
            "UNIQUE(`name`), UNIQUE(`fiscal_code`), UNIQUE(`bank_account`), UNIQUE(`hq_address`), " +
            "FOREIGN KEY(`customer_id`) REFERENCES `customer`(`id`)" +
        ");\n" +
        "CREATE TABLE IF NOT EXISTS `product` (" +
            "`id` INTEGER NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`price` REAL NOT NULL, " +
            "`stock` INTEGER NOT NULL DEFAULT 0, " +
            "PRIMARY KEY(`id`), " +
            "UNIQUE(`name`)" +
        ");\n" +
        "CREATE TABLE IF NOT EXISTS `invoice` (" +
            "`id` INTEGER NOT NULL, " +
            "`customer_id` INTEGER NOT NULL, " +
            "`date` TEXT NOT NULL, " +
            "PRIMARY KEY(`id`), " +
            "FOREIGN KEY(`customer_id`) REFERENCES `customer`(`id`)" +
        ");\n" +
        "CREATE TABLE IF NOT EXISTS `invoice_product` (" +
            "`invoice_id` INTEGER NOT NULL, " +
            "`product_id` INTEGER NOT NULL, " +
            "`product_price` REAL NOT NULL, " +
            "`quantity` INTEGER NOT NULL DEFAULT 1, " +
            "PRIMARY KEY(`invoice_id`, `product_id`), " +
            "FOREIGN KEY(`invoice_id`) REFERENCES `invoice`(`id`), " +
            "FOREIGN KEY(`product_id`) REFERENCES `product`(`id`)" +
        ");";

    /**
     * Initiate a new connection to SQLite3 database.
     * @throws SQLException see connect() method below.
     */
    public CRMDatabase() throws SQLException {
        connection = null;
        connect();
    }

    /**
     * Connects to SQLite3 database. If the database doesn't exists, it will create it.
     * @throws SQLException if a database access error occurs or
     *                      if the CRM directory couldn't be created in ProgramData.
     */
    public void connect() throws SQLException {
        File crmProgramDataFolder = new File(System.getenv("PROGRAMDATA") + "\\CRM");

        if (!crmProgramDataFolder.exists())
            if(!crmProgramDataFolder.mkdir())
                throw new SQLException("Couldn't create CRM directory in ProgramData.");

        String crmProgramDataFolderPath = crmProgramDataFolder.getAbsolutePath();
        crmProgramDataFolderPath = crmProgramDataFolderPath.replace('\\', '/');

        connection = DriverManager.getConnection("jdbc:sqlite:" + crmProgramDataFolderPath + "/crm.db");
        PreparedStatement statement = connection.prepareStatement(DatabaseCreation);

        statement.executeUpdate();
        statement.close();
    }

    /**
     * Disconnects from SQLite3 database.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Inserts a customer in `customer` table and in `individual` or `company` table,
     * depending on the run-time type of customer object.
     * @param customer must be a valid Individual or Company object.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public void insertCustomer(Customer customer) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String insertIntoCustomer = "INSERT INTO `customer`(`delivery_address`, `contact_number`) VALUES (?, ?);";

        connection.setAutoCommit(false);

        try {
            PreparedStatement statement = connection.prepareStatement(insertIntoCustomer);

            statement.setString(1, customer.getDeliveryAddress());
            statement.setString(2, customer.getContactNumber());

            if (statement.executeUpdate() != 1)
                throw new SQLException("No customer inserted!");

            int customerId = 0;
            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next())
                customerId = resultSet.getInt(1);

            resultSet.close();
            statement.close();

            if (customer instanceof Individual) {
                Individual individual = (Individual) customer;
                String insertIntoIndividual = "INSERT INTO `individual`(`customer_id`, `first_name`, `last_name`) VALUES (?, ?, ?);";
                statement = connection.prepareStatement(insertIntoIndividual);

                statement.setInt(1, customerId);
                statement.setString(2, individual.getFirstName());
                statement.setString(3, individual.getLastName());
                statement.executeUpdate();
                statement.close();
            } else if (customer instanceof Company) {
                Company company = (Company) customer;
                String insertIntoCompany = "INSERT INTO `company`(`customer_id`, `name`, `fiscal_code`, " +
                        "`bank_account`, `hq_address`) VALUES (?, ?, ?, ?, ?);";
                statement = connection.prepareStatement(insertIntoCompany);

                statement.setInt(1, customerId);
                statement.setString(2, company.getName());
                statement.setString(3, company.getFiscalCode());
                statement.setString(4, company.getBankAccount());
                statement.setString(5, company.getHeadquartersAddress());
                statement.executeUpdate();
                statement.close();
            } else
                throw new SQLException("Unknown customer instanceof.");

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    /**
     * Gets the customer id of a valid customer from the database.
     * If the customer has an entry in `customer` table but it doesn't
     * in `individual` or `company` table, this methods @throws InvalidCustomerException.
     * @param customer must be a valid customer in the database.
     * @return customer id if it is a valid customer in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidCustomerException if the customer is not in the database.
     */
    public int getCustomerId(Customer customer) throws CRMDBNotConnectedException,
            SQLException, InvalidCustomerException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectCustomerId = "SELECT `id` FROM `customer` WHERE `delivery_address`=? AND `contact_number`=?;";
        PreparedStatement statement = connection.prepareStatement(selectCustomerId);

        statement.setString(1, customer.getDeliveryAddress());
        statement.setString(2, customer.getDeliveryAddress());
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");

            if (customer instanceof Individual) {
                String selectIndividual = "SELECT `first_name`, `last_name` FROM `individual` WHERE `customer_id`=?;";
                PreparedStatement statement2 = connection.prepareStatement(selectIndividual);

                statement2.setInt(1, id);
                ResultSet resultSet2 = statement2.executeQuery();

                if (resultSet2.next()) {
                    if (resultSet2.getString("first_name").equals(((Individual)customer).getFirstName()) &&
                        resultSet2.getString("last_name").equals(((Individual)customer).getLastName())) {
                        resultSet2.close();
                        statement2.close();
                        resultSet.close();
                        statement.close();
                        return id;
                    }
                }

                resultSet2.close();
                statement2.close();
            } else if (customer instanceof Company) {
                String selectCompany = "SELECT `name`, `fiscal_code`, `bank_account`, `hq_address` " +
                        "FROM `company` WHERE `customer_id`=?;";
                PreparedStatement statement2 = connection.prepareStatement(selectCompany);

                statement2.setInt(1, id);
                ResultSet resultSet2 = statement2.executeQuery();

                if (resultSet2.next()) {
                    Company company = (Company)customer;

                    if (resultSet2.getString("name").equals(company.getName()) &&
                        resultSet2.getString("fiscal_code").equals(company.getFiscalCode()) &&
                        resultSet2.getString("bank_account").equals(company.getBankAccount()) &&
                        resultSet2.getString("hq_address").equals(company.getHeadquartersAddress())) {
                        resultSet2.close();
                        statement2.close();
                        resultSet.close();
                        statement.close();
                        return id;
                    }
                }
            }
        }

        resultSet.close();
        statement.close();
        throw new InvalidCustomerException();
    }

    /**
     * Insert a product in the database.
     * @param name is the name of the product. This is a unique key in the database.
     * @param price is the price of the product.
     * @param stock is the quantity of the product you have in stock.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public void insertProduct(String name, double price, int stock) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String insertProduct = "INSERT INTO `product`(`name`, `price`, `stock`) VALUES (?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(insertProduct);
        statement.setString(1, name);
        statement.setDouble(2, price);
        statement.setInt(3, stock);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Gets the product id of a valid product from the database.
     * @param productName must be a valid product name in the database.
     * @return product id if it is a valid product in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not in the database.
     */
    public int getProductId(String productName) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectProductId = "SELECT `id` FROM `product` WHERE `name`=?;";
        PreparedStatement statement = connection.prepareStatement(selectProductId);

        statement.setString(1, productName);
        ResultSet resultSet = statement.executeQuery();

        int productId = -1;

        if (resultSet.next())
            productId = resultSet.getInt("id");

        resultSet.close();
        statement.close();

        if (productId != -1)
            return productId;

        throw new InvalidProductException();
    }

    /**
     * Gets a product data using it's name.
     * @param productName must be a valid product name in the database.
     * @return an array of Object of size 4, containing the product data.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not in the database.
     */
    public Object[] getProductByName(String productName) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectedProduct = "SELECT * FROM `product` WHERE `name`=?;";
        PreparedStatement statement = connection.prepareStatement(selectedProduct);

        statement.setString(1, productName);
        ResultSet resultSet = statement.executeQuery();

        Object[] resultProduct = null;

        if (resultSet.next()) {
            resultProduct = new Object[4];
            resultProduct[0] = resultSet.getInt("id");
            resultProduct[1] = resultSet.getString("name");
            resultProduct[2] = resultSet.getDouble("price");
            resultProduct[3] = resultSet.getInt("stock");
        }

        resultSet.close();
        statement.close();

        if (resultProduct == null)
            throw new InvalidProductException();

        return resultProduct;
    }

    /**
     * Insert an invoice in the database.
     * @param customerId must be a valid customer id from the database.
     * @param products must be a matrix of n lines and 4 columns containing the products.
     *                 The first column is the product id.
     *                 The second column is the product name. This is not used.
     *                 The third column is the product price.
     *                 The fourth column is the product quantity.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidCustomerException if the customer specified in invoice object is not in the database.
     * @throws EmptyInvoiceException if the invoice doesn't have any products in it.
     * @throws InvalidProductException if the invoice contains a product that isn't in the database.
     */
    public void insertInvoice(int customerId, Object[][] products) throws CRMDBNotConnectedException, SQLException,
            InvalidCustomerException, EmptyInvoiceException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (products.length <= 0)
            throw new EmptyInvoiceException();

        if (products[0].length != 4)
            throw new InvalidProductException();

        String insertInvoice = "INSERT INTO `invoice`(`customer_id`, `date`) VALUES (?, CURRENT_TIMESTAMP);";
        String insertProduct = "INSERT INTO `invoice_product` VALUES (?, ?, ?, ?);";

        connection.setAutoCommit(false);

        try {
            PreparedStatement insertInvoiceStatement = connection.prepareStatement(insertInvoice);

            insertInvoiceStatement.setInt(1, customerId);

            if (insertInvoiceStatement.executeUpdate() != 1)
                throw new InvalidCustomerException();

            int invoiceId = 0;
            ResultSet resultSet = insertInvoiceStatement.getGeneratedKeys();

            if (resultSet.next())
                invoiceId = resultSet.getInt(1);

            resultSet.close();
            insertInvoiceStatement.close();

            for (Object[] product : products) {
                PreparedStatement insertInvoiceProductStatement = connection.prepareStatement(insertProduct);

                insertInvoiceProductStatement.setInt(1, invoiceId);
                insertInvoiceProductStatement.setInt(2, (Integer) product[0]);
                insertInvoiceProductStatement.setDouble(3, (Double) product[2]);
                insertInvoiceProductStatement.setInt(4, (Integer) product[3]);
                insertInvoiceProductStatement.executeUpdate();
                insertInvoiceProductStatement.close();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception exception) {
            // Catch both InvalidCustomerException and SQLException.
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    /**
     * Update customer.
     * @param customer must be an instance of Individual or Company with 'id' field valid in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidCustomerException if the 'id' field in customer doesn't exist in the database.
     */
    public void updateCustomer(Customer customer) throws CRMDBNotConnectedException, SQLException,
            InvalidCustomerException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (!isValidCustomer(customer.getId()))
            throw new InvalidCustomerException();

        String updateCustomer = "UPDATE `customer` SET `delivery_address`=?, `contact_number`=? WHERE `id`=?;";
        String updateIndividual = "UPDATE `individual` SET `first_name`=?, `last_name`=? WHERE `customer_id`=?;";
        String updateCompany = "UPDATE `company` SET `name`=?, `fiscal_code`=?, `bank_account`=?, `hq_address`=? WHERE `customer_id`=?;";

        connection.setAutoCommit(false);

        try {
            PreparedStatement statement = connection.prepareStatement(updateCustomer);

            statement.setString(1, customer.getDeliveryAddress());
            statement.setString(2, customer.getContactNumber());
            statement.setInt(3, customer.getId());
            statement.executeUpdate();
            statement.close();

            if (customer instanceof Individual) {
                Individual individual = (Individual)customer;
                statement = connection.prepareStatement(updateIndividual);

                statement.setString(1, individual.getFirstName());
                statement.setString(2, individual.getLastName());
                statement.setInt(3, customer.getId());
                statement.executeUpdate();
                statement.close();
            } else if (customer instanceof Company) {
                Company company = (Company)customer;
                statement = connection.prepareStatement(updateCompany);

                statement.setString(1, company.getName());
                statement.setString(2, company.getFiscalCode());
                statement.setString(3, company.getBankAccount());
                statement.setString(4, company.getHeadquartersAddress());
                statement.setInt(5, customer.getId());
                statement.executeUpdate();
                statement.close();
            } else
                throw new InvalidCustomerException();

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception exception) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw exception;
        }
    }

    /**
     * Update the price of a product.
     * @param productId must be a valid product id from the database.
     * @param newPrice must be greater than.
     *                 If the value is zero or less, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductPrice(int productId, double newPrice) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (newPrice <= 0.0)
            return;

        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (!isValidProduct(productId))
            throw new InvalidProductException();

        String updateProduct = "UPDATE `product` SET `price`=? WHERE `id`=?;";
        PreparedStatement statement = connection.prepareStatement(updateProduct);

        statement.setDouble(1, newPrice);
        statement.setInt(2, productId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Update the price of a product.
     * Wrapper for updateProductPrice(int, double).
     * @param productName must be a valid product name from the database.
     * @param newPrice must be greater than.
     *                 If the value is zero or less, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductPrice(String productName, double newPrice) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        updateProductPrice(getProductId(productName), newPrice);
    }

    /**
     * Update the stock of a product.
     * @param productId must be a valid product id from the database.
     * @param stock must be greater or equal with zero.
     *              If the value is less than zero, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductStock(int productId, int stock) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (stock < 0)
            return;

        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (!isValidProduct(productId))
            throw new InvalidProductException();

        String updateProduct = "UPDATE `product` SET `stock`=? WHERE `id`=?;";
        PreparedStatement statement = connection.prepareStatement(updateProduct);

        statement.setInt(1, stock);
        statement.setInt(2, productId);
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Update the stock of a product.
     * Wrapper for updateProductStock(int, int).
     * @param productName must be a valid product name from the database.
     * @param stock must be greater or equal with zero.
     *              If the value is less than zero, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductStock(String productName, int stock) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        updateProductStock(getProductId(productName), stock);
    }

    /**
     * Gets all the customers.
     * @return an ArrayList containing all the customers.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public List<Customer> getCustomers() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Customer> customers = new ArrayList<>();
        String getIndividuals = "SELECT i.customer_id, i.first_name, i.last_name, c.delivery_address, c.contact_number\n" +
                "FROM individual i JOIN customer c ON (c.id = i.customer_id);";
        String getCompanies = "SELECT co.customer_id, co.name, co.fiscal_code, co.bank_account, co.hq_address, " +
                "c.delivery_address, c.contact_number\n" +
                "FROM company co JOIN customer c ON (c.id = co.customer_id);";

        PreparedStatement statement = connection.prepareStatement(getIndividuals);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next())
            customers.add(new Individual(resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getInt("customer_id"),
                    resultSet.getString("delivery_address"), resultSet.getString("contact_number")));

        resultSet.close();
        statement.close();

        statement = connection.prepareStatement(getCompanies);
        resultSet = statement.executeQuery();

        while (resultSet.next())
            customers.add(new Company(resultSet.getString("name"),
                    resultSet.getString("fiscal_code"), resultSet.getString("bank_account"),
                    resultSet.getString("hq_address"), resultSet.getInt("customer_id"),
                    resultSet.getString("delivery_address"), resultSet.getString("contact_number")));

        resultSet.close();
        statement.close();
        return customers;
    }

    /**
     * Gets the number of individuals.
     * @return the number of individuals in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public int getIndividualsNo() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS IndividualsNo FROM `individual`;");
        ResultSet resultSet = statement.executeQuery();

        int individualsNo = 0;

        if (resultSet.next())
            individualsNo = resultSet.getInt("IndividualsNo");

        resultSet.close();
        statement.close();
        return individualsNo;
    }

    /**
     * Gets the number of companies.
     * @return the number of companies in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public int getCompaniesNo() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS CompaniesNo FROM `company`;");
        ResultSet resultSet = statement.executeQuery();

        int companiesNo = 0;

        if (resultSet.next())
            companiesNo = resultSet.getInt("CompaniesNo");

        resultSet.close();
        statement.close();
        return companiesNo;
    }

    /**
     * Gets the number of products.
     * @return the number of products in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public int getProductsNo() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS ProductsNo FROM `product`;");
        ResultSet resultSet = statement.executeQuery();

        int productsNo = 0;

        if (resultSet.next())
            productsNo = resultSet.getInt("ProductsNo");

        resultSet.close();
        statement.close();
        return productsNo;
    }

    /**
     * Gets all the individuals data.
     * @return a two dimensional array of Object, containing the data for each individual on a row.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getIndividuals() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        int individualsNo = getIndividualsNo();

        if (individualsNo <= 0)
            return null;

        int index = 0;
        Object[][] individualsData = new Object[individualsNo][MainWindow.individualsTableColumnNames.length];
        String selectIndividuals = "SELECT i.customer_id, i.first_name, i.last_name, " +
                "c.delivery_address, c.contact_number FROM individual i JOIN customer c ON (c.id = i.customer_id);";
        PreparedStatement statement = connection.prepareStatement(selectIndividuals);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            individualsData[index][0] = resultSet.getInt("customer_id");
            individualsData[index][1] = resultSet.getString("first_name");
            individualsData[index][2] = resultSet.getString("last_name");
            individualsData[index][3] = resultSet.getString("delivery_address");
            individualsData[index++][4] = resultSet.getString("contact_number");
        }

        resultSet.close();
        statement.close();
        return individualsData;
    }

    /**
     * Gets all the companies data.
     * @return a two dimensional array of Object, containing the data for each company on a row.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getCompanies() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        int companiesNo = getCompaniesNo();

        if (companiesNo <= 0)
            return null;

        int index = 0;
        Object[][] companiesData = new Object[companiesNo][MainWindow.companiesTableColumnNames.length];
        String selectCompanies = "SELECT co.customer_id, co.name, co.fiscal_code, " +
                "co.bank_account, co.hq_address, c.delivery_address, c.contact_number " +
                "FROM company co JOIN customer c ON (c.id = co.customer_id);";
        PreparedStatement statement = connection.prepareStatement(selectCompanies);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            companiesData[index][0] = resultSet.getInt("customer_id");
            companiesData[index][1] = resultSet.getString("name");
            companiesData[index][2] = resultSet.getString("fiscal_code");
            companiesData[index][3] = resultSet.getString("bank_account");
            companiesData[index][4] = resultSet.getString("hq_address");
            companiesData[index][5] = resultSet.getString("delivery_address");
            companiesData[index++][6] = resultSet.getString("contact_number");
        }

        resultSet.close();
        statement.close();
        return companiesData;
    }

    /**
     * Gets all the products data.
     * @return a two dimensional array of Object, containing the data for each product on a row.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getProducts() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        int productsNo = getProductsNo();

        if (productsNo <= 0)
            return null;

        int index = 0;
        Object[][] productsData = new Object[productsNo][MainWindow.productsTableColumnNames.length];
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `product`;");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            productsData[index][0] = resultSet.getInt("id");
            productsData[index][1] = resultSet.getString("name");
            productsData[index][2] = resultSet.getDouble("price");
            productsData[index++][3] = resultSet.getInt("stock");
        }

        resultSet.close();
        statement.close();
        return productsData;
    }

    /**
     * Gets the stock of a product.
     * @param productId must be a valid product id from the database.
     * @return the stock of the product if the product id is valid.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product id is invalid.
     */
    public int getProductStock(int productId) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT `stock` FROM `product` WHERE `id`=?");

        statement.setInt(1, productId);

        int stock = -1;
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next())
            stock = resultSet.getInt("stock");

        resultSet.close();
        statement.close();

        if (stock == -1)
            throw new InvalidProductException();

        return stock;
    }

    /**
     * Gets the stock of a product.
     * Wrapper for getProductStock(int).
     * @param productName must be a valid product name from the database.
     * @return the stock of the product if the product name is valid.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the product name is invalid.
     */
    public int getProductStock(String productName) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        return getProductStock(getProductId(productName));
    }

    /**
     * Checks whether a product id is valid or not.
     * @return true if the product id is in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isValidProduct(int productId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `product` WHERE `id`=?;");

        statement.setInt(1, productId);

        ResultSet resultSet = statement.executeQuery();
        boolean isValidProduct = resultSet.next();

        resultSet.close();
        statement.close();
        return isValidProduct;
    }

    /**
     * Checks whether a customer id is valid or not.
     * @return true if the customer id is in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isValidCustomer(int customerId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `customer` WHERE `id`=?;");

        statement.setInt(1, customerId);

        ResultSet resultSet = statement.executeQuery();
        boolean isValidCustomer = resultSet.next();

        resultSet.close();
        statement.close();
        return isValidCustomer;
    }

    /**
     * Checks whether an invoice id is valid or not.
     * @return true if the invoice id is in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isValidInvoice(int invoiceId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice` WHERE `id`=?;");

        statement.setInt(1, invoiceId);

        ResultSet resultSet = statement.executeQuery();
        boolean isValidInvoice = resultSet.next();

        resultSet.close();
        statement.close();
        return isValidInvoice;
    }

    /**
     * Search product by id.
     * @return a one dimensional array of Object, containing the data for the product.
     * @param productId is the uid of the product in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if the id passed as parameter doesn't exist in the database.
     */
    public Object[] getProduct(int productId) throws CRMDBNotConnectedException, SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (!isValidProduct(productId))
            throw new InvalidProductException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `product` WHERE `id`=?;");

        statement.setInt(1, productId);

        Object[] product = null;
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            product = new Object[MainWindow.productsTableColumnNames.length];
            product[0] = resultSet.getInt("id");
            product[1] = resultSet.getString("name");
            product[2] = resultSet.getDouble("price");
            product[3] = resultSet.getInt("stock");
        }

        resultSet.close();
        statement.close();

        if (product == null)
            throw new InvalidProductException();

        return product;
    }

    /**
     * Search product by name.
     * @return a two dimensional array of Object, containing the data for each product on a row.
     * @param namePattern the pattern to look for.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidProductException if no product was found.
     */
    public Object[][] getProduct(String namePattern) throws CRMDBNotConnectedException, SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> products = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `product` WHERE `name` LIKE ?;");

        statement.setString(1, "%" + namePattern + "%");

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] product = new Object[4];
            product[0] = resultSet.getInt("id");
            product[1] = resultSet.getString("name");
            product[2] = resultSet.getDouble("price");
            product[3] = resultSet.getInt("stock");
            products.add(product);
        }

        resultSet.close();
        statement.close();

        if (products.isEmpty())
            throw new InvalidProductException();

        Object[][] productsData = new Object[products.size()][MainWindow.productsTableColumnNames.length];

        for (int i = 0; i < products.size(); ++i)
            productsData[i] = products.get(i);

        return productsData;
    }

    /**
     * Search for the best selling product.
     * @return an uni-dimensional array of Object, containing the product data.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[] getBestSellingProduct() throws CRMDBNotConnectedException, SQLException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectBestSellingProduct = "SELECT `product_id`, sum(`quantity`) AS quantity FROM `invoice_product` GROUP BY `product_id`;";
        PreparedStatement statement = connection.prepareStatement(selectBestSellingProduct);
        ResultSet resultSet = statement.executeQuery();

        List<Object[]> data = new ArrayList<>();

        while (resultSet.next()) {
            Object[] rowData = new Object[2];
            rowData[0] = resultSet.getInt("product_id");
            rowData[1] = resultSet.getInt("quantity");
            data.add(rowData);
        }

        resultSet.close();
        statement.close();

        int limit = data.size();
        int max = Integer.MIN_VALUE;
        int maxPos = -1;

        for (int i = 0; i < limit; ++i) {
            int value = (Integer)data.get(i)[1];

            if (value > max) {
                max = value;
                maxPos = i;
            }
        }

        return maxPos >= 0 ? getProduct((Integer) data.get(maxPos)[0]) : null;
    }

    /**
     * Search for the best customer. The best customer is the one that earned us the most money.
     * @return a string with the name of the best customer.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public String getBestCustomer() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectBestCustomer = "SELECT `customer_id`, COUNT(`customer_id`) AS invoices FROM `invoice` GROUP BY `customer_id`;";
        PreparedStatement statement = connection.prepareStatement(selectBestCustomer);
        ResultSet resultSet = statement.executeQuery();

        List<Object[]> data = new ArrayList<>();

        while (resultSet.next()) {
            Object[] rowData = new Object[2];
            rowData[0] = resultSet.getInt("customer_id");
            rowData[1] = resultSet.getInt("invoices");
            data.add(rowData);
        }

        resultSet.close();
        statement.close();

        int limit = data.size();
        int max = Integer.MIN_VALUE;
        int maxPos = -1;

        for (int i = 0; i < limit; ++i) {
            int value = (Integer)data.get(i)[1];

            if (value > max) {
                max = value;
                maxPos = i;
            }
        }

        return maxPos >= 0 ? getCustomerName((Integer)data.get(maxPos)[0]) : null;
    }

    /**
     * Get the no. of invoices for each customer and the total money we earned from them.
     * @return a two-dimensional array containing the data about customers.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getCustomersPayments() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String selectCustomersPayments =
                "SELECT m.CustomerID, COUNT(m.InvoiceID) AS 'Invoices No.', sum(m.Price) AS 'Total earned from customer'\n" +
                "FROM (SELECT i.customer_id AS 'CustomerID', ip.invoice_id AS 'InvoiceID', sum(ip.product_price * ip.quantity) AS 'Price'\n" +
                "      FROM `invoice_product` ip JOIN `invoice` i ON (i.id = ip.invoice_id)\n" +
                "      GROUP BY ip.invoice_id) m\n" +
                "GROUP BY m.CustomerID;";

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(selectCustomersPayments);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] rowData = new Object[4];
            rowData[0] = resultSet.getInt("CustomerID");
            rowData[1] = getCustomerName((Integer)rowData[0]);
            rowData[2] = resultSet.getInt("Invoices No.");
            rowData[3] = resultSet.getDouble("Total earned from customer");
            data.add(rowData);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] rawData = new Object[data.size()][4];

        for (int i = 0; i < data.size(); ++i)
            rawData[i] = data.get(i);

        return rawData;
    }

    /**
     * Gets the name of a customer.
     * @param customerId represents the id of the customer.
     * @return the name of the customer. In case of an individual it is the concatenation between first name and last name.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    private String getCustomerName(int customerId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String customerName = null;

        String getIndividuals = "SELECT `first_name`, `last_name` FROM `individual` WHERE `customer_id`=?;";
        PreparedStatement statement = connection.prepareStatement(getIndividuals);

        statement.setInt(1, customerId);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            customerName = resultSet.getString("first_name");
            customerName = customerName + " " + resultSet.getString("last_name");
        }

        resultSet.close();
        statement.close();

        if (customerName == null) {
            statement = connection.prepareStatement("SELECT `name` FROM `company` WHERE `customer_id`=?;");

            statement.setInt(1, customerId);

            resultSet = statement.executeQuery();

            if (resultSet.next())
                customerName = resultSet.getString("name");
        }

        return customerName;
    }

    /**
     * Checks if an id represents a company.
     * @param companyId
     * @return true if the provided id is a company.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isCompany(int companyId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `company` WHERE `customer_id`=?;");

        statement.setInt(1, companyId);

        ResultSet resultSet = statement.executeQuery();
        boolean isCompany = resultSet.next();

        resultSet.close();
        statement.close();

        return isCompany;
    }

    /**
     * Checks if an id represents an individual.
     * @param individualId
     * @return true if the provided id is an individual.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isIndividual(int individualId) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `individual` WHERE `customer_id`=?;");

        statement.setInt(1, individualId);

        ResultSet resultSet = statement.executeQuery();
        boolean isIndividual = resultSet.next();

        resultSet.close();
        statement.close();

        return isIndividual;
    }

    /**
     * Get an Object with the data of an Invoice by an UID
     * @param invoiceUID
     * @return Object[3] if the UID is valid and null otherwise.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[] getInvoiceByUid(int invoiceUID) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        Object[] invoice = null;

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice` WHERE `id`=?;");

        statement.setInt(1, invoiceUID);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
        }

        resultSet.close();
        statement.close();

        return invoice;
    }

    /**
     * Get multiple Objects with the data of invoices by an customer ID
     * @param customerName
     * @return Object[][3] if the customerID is valid and null otherwise.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getInvoicesByCustomerName(String customerName) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        int customerID = getCustomerIdByName(customerName);
        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice` WHERE `customer_id`=?;");

        statement.setInt(1, customerID);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Object[] invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
            data.add(invoice);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] invoices = new Object[data.size()][3];

        for (int i = 0; i < data.size(); ++i)
            invoices[i] = data.get(i);

        return invoices;
    }

    /**
     * Get the ID of a customer, searching by his name
     * @param customerName
     * @return int if the customerID is valid and -1 otherwise.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public int getCustomerIdByName(String customerName) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        int customerId = -1;

        String getIndividuals = "SELECT `customer_id` FROM `individual` WHERE `first_name`=? OR `last_name`=?;";
        PreparedStatement statement = connection.prepareStatement(getIndividuals);

        statement.setString(1, customerName);
        statement.setString(2, customerName);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next())
            customerId = resultSet.getInt("customer_id");

        resultSet.close();
        statement.close();

        if (customerId == -1) {
            statement = connection.prepareStatement("SELECT `customer_id` FROM `company` WHERE `name`=?;");

            statement.setString(1, customerName);

            resultSet = statement.executeQuery();

            if (resultSet.next())
                customerId = resultSet.getInt("customer_id");

            resultSet.close();
            statement.close();
        }

        return customerId;
    }

    /**
     * Get multiple Objects with the data of invoices by date
     * @param date
     * @return Object[][3] if the customerID is valid and null otherwise.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getInvoicesByDate(String date) throws CRMDBNotConnectedException, SQLException{
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice` WHERE `date` LIKE ?;");

        statement.setString(1, '%' + date + '%');

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
            data.add(invoice);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] invoices = new Object[data.size()][3];

        for (int i = 0; i < data.size(); ++i)
            invoices[i] = data.get(i);

        return invoices;
    }

    /**
     * Get all the invoices as Object[][]
     * @return Object[][]
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getInvoices() throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice`;");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
            data.add(invoice);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] invoices = new Object[data.size()][3];

        for (int i = 0; i < data.size(); ++i)
            invoices[i] = data.get(i);

        return invoices;
    }

    /**
     * Get all the invoices for companies as Object[][]
     * @return Object[][]
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getCompaniesInvoices() throws SQLException, CRMDBNotConnectedException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT inv.id, inv.customer_id, inv.date \n" +
                "FROM invoice inv JOIN company c ON (inv.customer_id = c.Customer_id);");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
            data.add(invoice);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] invoices = new Object[data.size()][3];

        for (int i = 0; i < data.size(); ++i)
            invoices[i] = data.get(i);

        return invoices;
    }

    /**
     * Get all the invoices for individuals as Object[][]
     * @return Object[][]
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getIndividualsInvoices() throws SQLException, CRMDBNotConnectedException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT inv.id, inv.customer_id, inv.date \n" +
                "FROM invoice inv JOIN individual i ON (inv.customer_id = i.customer_id);");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] invoice = new Object[3];
            invoice[0] = resultSet.getInt("id");
            invoice[1] = resultSet.getInt("customer_id");
            invoice[2] = resultSet.getString("date");
            data.add(invoice);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] invoices = new Object[data.size()][3];

        for (int i = 0; i < data.size(); ++i)
            invoices[i] = data.get(i);

        return invoices;
    }

    /**
     * Get all the products from the invoice with the specified parameter as Object[][]
     * @param invoiceUID
     * @return Object[][]
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException if a database access error occurs.
     */
    public Object[][] getProductsOfInvoice(int invoiceUID) throws SQLException, CRMDBNotConnectedException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        List<Object[]> data = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `invoice_product` WHERE `invoice_id`=?;");

        statement.setInt(1, invoiceUID);

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Object[] product = new Object[4];
            product[0] = resultSet.getInt("invoice_id");
            product[1] = resultSet.getInt("product_id");
            product[2] = resultSet.getDouble("product_price");
            product[3] = resultSet.getInt("quantity");
            data.add(product);
        }

        resultSet.close();
        statement.close();

        if (data.isEmpty())
            return null;

        Object[][] products = new Object[data.size()][4];

        for (int i = 0; i < data.size(); ++i)
            products[i] = data.get(i);

        return products;
    }
}
