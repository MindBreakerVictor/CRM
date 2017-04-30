package crm;

import java.util.ArrayList;
import java.util.List;

public class Accounting {

    private static List<Customer> customers;
    private static List<Invoice> invoices;

    static {
        customers = new ArrayList<>();
        invoices = new ArrayList<>();
    }

    public static void addCustomer(Customer customer) { customers.add(customer); }

    public static void addInvoice(Invoice invoice) { invoices.add(invoice); }

    public static String displayCustomers() {
        StringBuilder customersString = new StringBuilder();

        for (Customer customer : customers)
            customersString.append(customer.toString());

        return customersString.toString();
    }

    public static Customer getCustomerById(int id) {
        for (Customer customer : customers)
            if (customer.getId() == id)
                return customer;

        return null;
    }
}
