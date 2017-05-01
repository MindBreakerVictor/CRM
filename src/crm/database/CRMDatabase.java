package crm.database;

import crm.*;

import java.sql.*;
import java.util.Map;

public class CRMDatabase {

    private Connection connection;

    /* TODO: Add unique key constrains. */
    private static final String DatabaseCreation = "PRAGMA foreign_keys=ON;\n" +
        "CREATE TABLE IF NOT EXISTS `customer` (" +
            "`id` INTEGER NOT NULL, " +
            "`delivery_address` TEXT NOT NULL, " +
            "`contact_number` TEXT NOT NULL, " +
            "PRIMARY KEY(`id`)" +
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

    public CRMDatabase() { connection = null; }

    /**
     * Connects to SQLite3 database. If the database doesn't exists, it will create it.
     * @throws SQLException
     */
    public void connect() throws SQLException {
        String url = "jdbc:sqlite:C/ProgramData/CRM/crm.db";
        connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();
        statement.execute(DatabaseCreation);
        statement.close();
    }

    /**
     * Disconnects from SQLite3 database.
     * @throws SQLException
     */
    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Inserts a customer in `customer` table and in `individual` or `company` table,
     * depending on the run-time type of customer object.
     * TODO: Make the whole operation transactional, so we're able to rollback in case the second insert fails.
     * @param customer must be a valid Individual or Company object.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     */
    public void insertCustomer(Customer customer) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String insertIntoCustomer = "INSERT INTO `customer`(`delivery_address`, `contact_number`) VALUES (?, ?);";
        PreparedStatement statement = connection.prepareStatement(insertIntoCustomer);

        statement.setString(1, customer.getDeliveryAddress());
        statement.setString(2, customer.getContactNumber());
        statement.executeUpdate();

        int customerId = 0;
        ResultSet resultSet = statement.getGeneratedKeys();

        if (resultSet.next())
            customerId = resultSet.getInt(1);
        //else rollback previous insert and throw exception

        if (customer instanceof Individual) {
            Individual individual = (Individual)customer;
            String sql2 = "INSERT INTO `individual`(`customer_id`, `first_name`, `last_name`) VALUES (?, ?, ?);";
            PreparedStatement statement2 = connection.prepareStatement(sql2);

            statement2.setInt(1, customerId);
            statement2.setString(2, individual.getFirstName());
            statement2.setString(3, individual.getLastName());
            statement2.executeUpdate();
            statement2.close();
        } else if (customer instanceof Company) {
            Company company = (Company)customer;
            String insertIntoCompany = "INSERT INTO `company`(`customer_id`, `name`, `fiscal_code`, " +
                    "`bank_account`, `hq_address`) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement statement2 = connection.prepareStatement(insertIntoCompany);

            statement2.setInt(1, customerId);
            statement2.setString(2, company.getName());
            statement2.setString(3, company.getFiscalCode());
            statement2.setString(4, company.getBankAccount());
            statement2.setString(5, company.getHeadquartersAddress());
            statement2.executeUpdate();
            statement2.close();
        }

        resultSet.close();
        statement.close();
    }

    /**
     * Gets the customer id of a valid customer from the database.
     * If the customer has an entry in `customer` table but it doesn't
     * in `individual` or `company` table, this methods @throws InvalidCustomerException.
     * @param customer must be a valid customer in the database.
     * @return customer id if it is a valid customer in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     * @throws InvalidCustomerException if the customer is not in the database.
     */
    public int getCustomerId(Customer customer) throws CRMDBNotConnectedException, SQLException, InvalidCustomerException {
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
     * @param product must be a valid product.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     */
    public void insertProduct(Product product) throws CRMDBNotConnectedException, SQLException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        String insertProduct = "INSERT INTO `product`(`name`, `price`, `stock`) VALUES (?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(insertProduct);
        statement.setString(1, product.getName());
        statement.setDouble(2, product.getPrice());
        statement.setInt(3, product.getStock());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Gets the product id of a valid product from the database.
     * @param productName must be a valid product in the database.
     * @return product id if it is a valid product in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     * @throws InvalidProductException if the product is not in the database.
     */
    public int getProductId(String productName) throws CRMDBNotConnectedException, SQLException, InvalidProductException {
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

    public int getProductId(Product product) throws CRMDBNotConnectedException, SQLException, InvalidProductException {
        return getProductId(product.getName());
    }

    /**
     * Insert an invoice in the database.
     * TODO: This should be done transactional.
     * @param invoice must be a valid invoice.
     *                The customer object in invoice must be already in the database.
     *                The products hash map of invoice cannot be empty.
     *                The invoice's products must be already in the database.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     * @throws InvalidCustomerException if the customer specified in invoice object is not in the database.
     * @throws EmptyInvoiceException if the invoice doesn't have any products in it.
     * @throws InvalidProductException if the invoice contains a product that isn't in the database.
     */
    public void insertInvoice(Invoice invoice) throws CRMDBNotConnectedException, SQLException,
            InvalidCustomerException, EmptyInvoiceException, InvalidProductException {
        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        if (invoice.getProducts().isEmpty())
            throw new EmptyInvoiceException();

        int customerId = getCustomerId(invoice.getCustomer());

        String insertInvoice = "INSERT INTO `invoice`(`customer_id`, `date`) VALUES (?, ?);";
        String insertProduct = "INSERT INTO `invoice_product` VALUES (?, ?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(insertInvoice);

        statement.setInt(1, customerId);
        statement.setString(2, invoice.getDate());
        statement.executeUpdate();

        int invoiceId = 0;
        ResultSet resultSet = statement.getGeneratedKeys();

        if (resultSet.next())
            invoiceId = resultSet.getInt(1);

        statement.close();

        for (Map.Entry<Product, Integer> entry : invoice.getProducts().entrySet()) {
            statement = connection.prepareStatement(insertProduct);

            statement.setInt(1, invoiceId);
            statement.setInt(2, getProductId(entry.getKey()));
            statement.setDouble(3, entry.getKey().getPrice());
            statement.setInt(4, entry.getValue());
            statement.executeUpdate();
            statement.close();
        }
    }

    /**
     * Update the price and stock of a product.
     * @param productName must be a product name of a product that is already in the database.
     * @param newPrice must be greater than.
     *                 If the value is zero or less, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductPrice(String productName, double newPrice) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (newPrice <= 0.0)
            return;

        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        /*
         * We can also do the lookup by name, but we use id to
         * throw InvalidProductException if the product is not in the database.
         */
        String updateProduct = "UPDATE `product` SET `price`=? WHERE `id`=?;";
        PreparedStatement statement = connection.prepareStatement(updateProduct);

        statement.setDouble(1, newPrice);
        statement.setInt(2, getProductId(productName));
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Update the price and stock of a product.
     * @param productName must be a product name of a product that is already in the database.
     * @param stock must be greater or equal with zero.
     *              If the value is less than zero, then the call is a no-op.
     * @throws CRMDBNotConnectedException if the database is not connected. You must call connect() first.
     * @throws SQLException
     * @throws InvalidProductException if the product is not already in the database.
     */
    public void updateProductStock(String productName, int stock) throws CRMDBNotConnectedException,
            SQLException, InvalidProductException {
        if (stock < 0)
            return;

        if (connection == null || connection.isClosed())
            throw new CRMDBNotConnectedException();

        /*
         * We can also do the lookup by name, but we use id to
         * throw InvalidProductException if the product is not in the database.
         */
        String updateProduct = "UPDATE `product` SET `stock`=? WHERE `id`=?;";
        PreparedStatement statement = connection.prepareStatement(updateProduct);

        statement.setInt(1, stock);
        statement.setInt(2, getProductId(productName));
        statement.executeUpdate();
        statement.close();
    }
}
