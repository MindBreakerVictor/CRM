package crm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Invoice {

    private int uid;
    private String date;
    private Customer customer;
    private HashMap<Product, Integer> products;

    private static int currentId;

    static {
        currentId = 0;
    }

    {
        uid = ++currentId;
        products = new HashMap<>();
    }

    public Invoice() { }

    public Invoice(Customer customer) {
        this.customer = customer;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public Customer getCustomer() { return customer; }

    public void setCustomer(Customer customer) { this.customer = customer; }

    public HashMap<Product, Integer> getProducts() {
        return products;
    }

    public void addProduct(Product p, int quantity) {
        products.put(p, quantity);
    }

    public void removeProduct(Product p, int quantity) { }

    public int getId() {
        return uid;
    }

    public void setId(int uid) { this.uid = uid; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set entrySet = products.entrySet();

        for (Object entry : entrySet) {
            Map.Entry mapEntry = (Map.Entry)entry;
            sb.append(mapEntry.getValue().toString());
            sb.append(", ");
            sb.append(mapEntry.getKey().toString());
            sb.append(", ");
        }

        return uid + ", " + customer.toString() + ", " + sb;
    }
}
