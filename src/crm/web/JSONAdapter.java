package crm.web;

import crm.data.Company;
import crm.data.Customer;
import crm.data.Individual;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class JSONAdapter {

    public void insertCustomer(Customer customer) {
        JSONObject object = new JSONObject();
        object.put("deliveryAddress", customer.getDeliveryAddress());
        object.put("contactNumber", customer.getContactNumber());
        if (customer instanceof Individual) {
            object.put("type", 1);
            object.put("firstName", ((Individual) customer).getFirstName());
            object.put("lastName", ((Individual) customer).getLastName());
        } else {
            if (customer instanceof Company) {
                object.put("type", 2);
                object.put("name", ((Company) customer).getName());
                object.put("fiscalCode", ((Company) customer).getFiscalCode());
                object.put("bankAccount", ((Company) customer).getBankAccount());
                object.put("headquartersAddress", ((Company) customer).getHeadquartersAddress());
            }
        }
        object.put("operation", "insertCustomer");

        //TODO Server Call
    }

    public void updateCustomer(Customer customer) {
        JSONObject object = new JSONObject();
        if (customer instanceof Individual) {
            object.put("type", 1);
            object.put("firstName", ((Individual) customer).getFirstName());
            object.put("lastName", ((Individual) customer).getLastName());
        } else {
            if (customer instanceof Company) {
                object.put("type", 2);
                object.put("name", ((Company) customer).getName());
                object.put("fiscalCode", ((Company) customer).getFiscalCode());
                object.put("bankAccount", ((Company) customer).getBankAccount());
                object.put("headquartersAddress", ((Company) customer).getHeadquartersAddress());
            }
        }
        object.put("operation", "updateCustomer");

        //TODO Server Call
    }

    public Object[][] getIndividuals() {
        JSONObject object = new JSONObject();
        object.put("operation", "getIndividuals");

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
                    JSONArray individuals = output.getJSONArray("individuals");

                    Object[][] result = new Object[individuals.length()][5];

                    for (int i = 0; i < individuals.length(); i++) {
                        result[i][0] = individuals.getJSONObject(i).getInt("customerId");
                        result[i][1] = individuals.getJSONObject(i).getString("firstName");
                        result[i][2] = individuals.getJSONObject(i).getString("lastName");
                        result[i][3] = individuals.getJSONObject(i).getString("deliveryAddress");
                        result[i][4] = individuals.getJSONObject(i).getString("contactNumber");
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

    public Object[][] getCompanies() {
        JSONObject object = new JSONObject();
        object.put("operation", "getCompanies");

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
                    JSONArray companies = output.getJSONArray("companies");

                    Object[][] result = new Object[companies.length()][7];

                    for (int i = 0; i < companies.length(); i++) {
                        result[i][0] = companies.getJSONObject(i).getInt("customerId");
                        result[i][1] = companies.getJSONObject(i).getString("name");
                        result[i][2] = companies.getJSONObject(i).getString("fiscalCode");
                        result[i][3] = companies.getJSONObject(i).getString("bankAccount");
                        result[i][3] = companies.getJSONObject(i).getString("headquartersAddress");
                        result[i][3] = companies.getJSONObject(i).getString("deliveryAddress");
                        result[i][4] = companies.getJSONObject(i).getString("contactNumber");
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

    public Object[] getBestSellingProduct() {
        JSONObject object = new JSONObject();
        object.put("operation", "getBestSellingProduct");

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
            e.printStackTrace();
        }

        return null;
    }

    public String getBestCustomer() {
        JSONObject object = new JSONObject();
        object.put("operation", "getBestCustomer");

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

                    int type = output.getInt("type");
                    if (type == 1) {
                        String firstName = output.getString("firstName");
                        String lastName = output.getString("lastName");

                        return firstName + " " + lastName;
                    } else {
                        String name = output.getString("name");

                        return name;
                    }
                } else
                    return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object[][] getCustomersPayments() {
        JSONObject object = new JSONObject();
        object.put("operation", "getCustomersPayments");

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
                    JSONArray payments = output.getJSONArray("payments");

                    Object[][] result = new Object[payments.length()][4];

                    for (int i = 0; i < payments.length(); i++) {
                        result[i][0] = payments.getJSONObject(i).getInt("customerId");
                        result[i][1] = payments.getJSONObject(i).getString("customerName");
                        result[i][2] = payments.getJSONObject(i).getInt("invoicesNo");
                        result[i][3] = payments.getJSONObject(i).getDouble("total");
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

    public boolean isCompany(int id) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("operation", "isCompany");

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

                    String result = output.getString("boolean");

                    return result.equals("true");

                } else
                    return false;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    public boolean isIndividual(int id) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("operation", "isIndividual");

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

                    String result = output.getString("boolean");

                    return result.equals("true");

                } else
                    return false;
            }
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    public Object[][] getCompaniesInvoices() {
        JSONObject object = new JSONObject();
        object.put("operation", "getCompaniesInvoices");

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

    public Object[][] getIndividualsInvoices() {
        JSONObject object = new JSONObject();
        object.put("operation", "getIndividualsInvoices");

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

    public Object[] getInvoiceById(int id) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("operation", "getInvoiceById");

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

                    Object[] result = new Object[3];

                    result[0] = output.getInt("id");
                    result[1] = output.getInt("customerId");
                    result[2] = output.getString("date");

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

    public Object[][] getInvoicesByCustomerName(String name) {
        JSONObject object = new JSONObject();
        object.put("name" , name);
        object.put("operation", "getInvoiceByCustomerName");

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

    public Object[][] getInvoicesByDate(String date) {
        JSONObject object = new JSONObject();
        object.put("date" , date);
        object.put("operation", "getInvoiceByDate");

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

    public Object[][] getProductsOfInvoice(int id) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("operation", "getProductsOfInvoice");

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
                    JSONArray products = output.getJSONArray("products");

                    Object[][] result = new Object[products.length()][5];

                    for (int i = 0; i < products.length(); i++) {
                        result[i][0] = products.getJSONObject(i).getInt("invoiceId");
                        result[i][1] = products.getJSONObject(i).getInt("productId");
                        result[i][2] = products.getJSONObject(i).getDouble("price");
                        result[i][3] = products.getJSONObject(i).getInt("quantity");
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

    public void insertProduct(String name, Double price, int quantity) {
        JSONObject object = new JSONObject();
        object.put("name" , name);
        object.put("price" , price);
        object.put("quantity" , quantity);
        object.put("operation", "insertProduct");

        //TODO Server Call
    }

    public void updateProductPrice(int id, Double price) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("price" , price);
        object.put("operation", "updateProductPrice");

        //TODO Server Call
    }

    public void updateProductStock(int id, int quantity) {
        JSONObject object = new JSONObject();
        object.put("id" , id);
        object.put("quantity" , quantity);
        object.put("operation", "updateProductStock");

        //TODO Server Call
    }

    public Object[][] getProducts() {
        JSONObject object = new JSONObject();
        object.put("operation", "getProducts");

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
                    JSONArray products = output.getJSONArray("products");

                    Object[][] result = new Object[products.length()][4];

                    for (int i = 0; i < products.length(); i++) {
                        result[i][0] = products.getJSONObject(i).getInt("id");
                        result[i][1] = products.getJSONObject(i).getString("name");
                        result[i][2] = products.getJSONObject(i).getDouble("price");
                        result[i][3] = products.getJSONObject(i).getInt("stock");
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

    public Object[][] getProduct(String name) {
        JSONObject object = new JSONObject();
        object.put("name", name);
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
                    JSONArray products = output.getJSONArray("products");

                    Object[][] result = new Object[products.length()][4];

                    for (int i = 0; i < products.length(); i++) {
                        result[i][0] = products.getJSONObject(i).getInt("id");
                        result[i][1] = products.getJSONObject(i).getString("name");
                        result[i][2] = products.getJSONObject(i).getDouble("price");
                        result[i][3] = products.getJSONObject(i).getInt("stock");
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

