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
            "`stock` INT NOT NULL DEFAULT 0, " +
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

    public CRMDatabase() throws SQLException {
        connection = null;
        connect();
    }

    /**
     * Connects to SQLite3 database. If the database doesn't exists, it will create it.
     * @throws SQLException if a database access error occurs.
     */
    public void connect() throws SQLException {
        File crmProgramDataFolder = new File(System.getenv("PROGRAMDATA") + "\\CRM");

        if (!crmProgramDataFolder.exists())
            crmProgramDataFolder.mkdir();

        String crmProgramDataFolderPath = crmProgramDataFolder.getAbsolutePath();
        crmProgramDataFolderPath = crmProgramDataFolderPath.replace('\\', '/');

        connection = DriverManager.getConnection("jdbc:sqlite:" + crmProgramDataFolderPath + "/crm.db");
        Statement statement = connection.createStatement();

        statement.executeUpdate(DatabaseCreation);
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
     * TODO: This should be done transactional.
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

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(getIndividuals);

        while (resultSet.next())
            customers.add(new Individual(resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getInt("customer_id"),
                    resultSet.getString("delivery_address"), resultSet.getString("contact_number")));

        resultSet.close();
        statement.close();

        statement = connection.createStatement();
        resultSet = statement.executeQuery(getCompanies);

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

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS IndividualsNo FROM individual;");

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

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS CompaniesNo FROM company;");

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

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS ProductsNo FROM product;");

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
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT i.customer_id, i.first_name, i.last_name, " +
                "c.delivery_address, c.contact_number FROM individual i JOIN customer c ON (c.id = i.customer_id);");

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
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT co.customer_id, co.name, co.fiscal_code, " +
                "co.bank_account, co.hq_address, c.delivery_address, c.contact_number " +
                "FROM company co JOIN customer c ON (c.id = co.customer_id);");

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
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM product;");

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

        ResultSet resultSet = statement.executeQuery();
        int stock = -1;

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
     * @throws SQLException
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
}
