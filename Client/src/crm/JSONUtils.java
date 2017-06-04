package crm;

import crm.data.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

public class JSONUtils {

    public static String insertCustomer(Customer customer) throws InvalidCustomerException {
        JSONObject json = new JSONObject();

        if (customer instanceof Individual) {
            json.put("type", 1);
            json.put("firstName", ((Individual)customer).getFirstName());
            json.put("lastName", ((Individual)customer).getLastName());
        } else if (customer instanceof Company) {
            json.put("type", 2);
            json.put("name", ((Company)customer).getName());
            json.put("fiscalCode", ((Company)customer).getFiscalCode());
            json.put("bankAccount", ((Company)customer).getBankAccount());
            json.put("headquartersAddress", ((Company)customer).getHeadquartersAddress());
        } else
            throw new InvalidCustomerException();

        json.put("deliveryAddress", customer.getDeliveryAddress());
        json.put("contactNumber", customer.getContactNumber());
        json.put("operation", "insertCustomer");

        return json.toString();
    }

    public static String updateCustomer(Customer customer) throws InvalidCustomerException {
        JSONObject json = new JSONObject();

        if (customer instanceof Individual) {
            json.put("type", 1);
            json.put("firstName", ((Individual) customer).getFirstName());
            json.put("lastName", ((Individual) customer).getLastName());
        } else if (customer instanceof Company) {
            json.put("type", 2);
            json.put("name", ((Company) customer).getName());
            json.put("fiscalCode", ((Company) customer).getFiscalCode());
            json.put("bankAccount", ((Company) customer).getBankAccount());
            json.put("headquartersAddress", ((Company) customer).getHeadquartersAddress());
        } else
            throw new InvalidCustomerException();

        json.put("id", customer.getId());
        json.put("deliveryAddress", customer.getDeliveryAddress());
        json.put("contactNumber", customer.getContactNumber());
        json.put("operation", "updateCustomer");

        return json.toString();
    }

    public static Object[][] getIndividuals(String individualsJSON) {
    }

    public static Object[][] getCompanies(String companiesJSON) {
    }

    public static Object[] getBestSellingProduct(String bestSellingProductJSON) {
    }

    public static String getBestCustomer(String bestCustomerJSON) {
    }

    public static Object[][] getCustomersPayments(String customersPaymentsJSON) {
    }

    public static boolean isCompany(int id) {
    }

    public static boolean isIndividual(int id) {
    }

    public static Object[][] getCompaniesInvoices(String companiesInvoicesJSON) {
    }

    public static Object[][] getIndividualsInvoices(String individualsInvoicesJSON) {
    }

    public static Object[] getInvoiceById(int id) {
    }

    public static Object[][] getInvoicesByCustomerName(String name) {
    }

    public static Object[][] getInvoicesByDate(String date) {
    }

    public static Object[][] getProductsOfInvoice(int id) {
    }

    public static String insertProduct(String name, Double price, int quantity) {
        JSONObject json = new JSONObject();
        json.put("name" , name);
        json.put("price" , price);
        json.put("quantity" , quantity);
        json.put("operation", "insertProduct");
        return json.toString();
    }

    public static String updateProductPrice(int id, Double price) {
        JSONObject json = new JSONObject();
        json.put("id" , id);
        json.put("price" , price);
        json.put("operation", "updateProductPrice");
        return json.toString();
    }

    public static String updateProductStock(int id, int quantity) {
        JSONObject json = new JSONObject();
        json.put("id" , id);
        json.put("quantity" , quantity);
        json.put("operation", "updateProductStock");
        return json.toString();
    }

    public static Object[][] getProducts() {
    }

    public Object[][] getProduct(String name) {
    }

    public Object[] getProduct(int id) {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("operation", "getProduct");

        //TODO Server Call

        try {
            URL myURL = new URL("http://");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()))) {
                String inputLine;
                inputLine = in.readLine();

                if (inputLine != null) {
                    JSONObject output = new JSONObject(inputLine);

                    Object[] result = new Object[4];

                    result[0] = output.getInt("id");
                    result[1] = output.getString("name");
                    result[2] = output.getDouble("price");
                    result[3] = output.getInt("stock");

                    return result;

                } else
                    return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public Object[] getProductByName(String name) {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("operation", "getProductByName");

        //TODO Server Call

        try {
            URL myURL = new URL("http://");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()))) {
                String inputLine;
                inputLine = in.readLine();

                if (inputLine != null) {
                    JSONObject output = new JSONObject(inputLine);

                    Object[] result = new Object[4];

                    result[0] = output.getInt("id");
                    result[1] = output.getString("name");
                    result[2] = output.getDouble("price");
                    result[3] = output.getInt("stock");

                    return result;

                } else
                    return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public void insertInvoice(int customerId, Object[][] products) {
        JSONObject object = new JSONObject();
        object.put("customerId", customerId);
        JSONArray array = new JSONArray();
        for (int i = 0 ; i < products.length; i++) {
            JSONObject product = new JSONObject();
            product.put("id", products[i][0]);
            product.put("name", products[i][1]);
            product.put("price", products[i][2]);
            product.put("quantity", products[i][3]);
            array.put(product);
        }
        object.put("products", array);
        object.put("operation", "insertInvoice");

        //TODO Server Call

    }

    public int getProductStock( int id) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("operation", "getProductStock");

        //TODO Server Call

        try {
            URL myURL = new URL("http://");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()))) {
                String inputLine;
                inputLine = in.readLine();

                if (inputLine != null) {
                    JSONObject output = new JSONObject(inputLine);

                    int result;

                    result = output.getInt("stock");
                    return result;

                } else
                    return 0;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return 0;
    }

    public List<Customer> getCustomers() {
        JSONObject object = new JSONObject();
        object.put("operation", "getCustomers");

        //TODO Server Call

        try {
            URL myURL = new URL("http://");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()))) {
                String inputLine;
                inputLine = in.readLine();

                if (inputLine != null) {
                    JSONObject output = new JSONObject(inputLine);
                    JSONArray results = output.getJSONArray("results");

                    List<Customer> customers = new ArrayList<>();

                    for (int i = 0; i < results.length(); i++) {
                        Customer customer;
                        if (results.getJSONObject(i).getInt("type") == 1) {
                            String firstName = results.getJSONObject(i).getString("firstName");
                            String lastName = results.getJSONObject(i).getString("lastName");
                            int customerId = results.getJSONObject(i).getInt("customerId");
                            String deliveryAddress = results.getJSONObject(i).getString("deliveryAddress");
                            String contactNumber = results.getJSONObject(i).getString("contactNumber");

                            customer = new Individual(firstName, lastName, customerId, deliveryAddress, contactNumber);
                            customers.add(customer);
                        } else {
                            String name = results.getJSONObject(i).getString("name");
                            String fiscalCode = results.getJSONObject(i).getString("fiscalCode");
                            String bankAccount = results.getJSONObject(i).getString("bankAccount");
                            String hqAddress = results.getJSONObject(i).getString("headquartersAddress");
                            int customerId  =results.getJSONObject(i).getInt("customerId");
                            String deliveryAddress = results.getJSONObject(i).getString("deliveryAddress");
                            String contactNumber = results.getJSONObject(i).getString("contactNumber");

                            customer = new Company(name, fiscalCode, bankAccount, hqAddress, customerId, deliveryAddress, contactNumber);
                            customers.add(customer);
                        }
                    }

                    return customers;

                } else
                    return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public Object[][] getInvoices() {
        JSONObject object = new JSONObject();
        object.put("operation", "getInvoices");

        //TODO Server Call
        try {
            URL myURL = new URL("http://");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()))) {
                String inputLine;
                inputLine = in.readLine();

                if (inputLine != null) {
                    JSONObject output = new JSONObject(inputLine);
                    JSONArray invoices = output.getJSONArray("invoices");

                    Object[][] result = new Object[invoices.length()][3];

                    for (int i = 0; i < invoices.length(); i++) {
                        result[i][0] = invoices.getJSONObject(i).getInt("id");
                        result[i][1] = invoices.getJSONObject(i).getInt("customerId");
                        result[i][2] = invoices.getJSONObject(i).getString("date");
                    }

                    return result;

                } else
                    return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

}

