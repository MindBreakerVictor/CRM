package crm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robert Tanase on 30-Apr-17.
 */
public class Invoice {

    private Customer customer;
    private HashMap<Product, Integer> products = new HashMap<>();
    private static int id;
    private int UID;
    static {
        id = 0;
    }
    {
        UID = ++id;
    }

    public Invoice() {
    }

    public Invoice(Customer customer) {
        this.customer = customer;
    }

    public void addProduct (Product p, Integer quantity) {
        products.put(p, quantity);
    }

    public HashMap<Product, Integer> getProducts() {
        return products;
    }

    public int getId() {
        return UID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set entry = products.entrySet();
        Iterator i = entry.iterator();

        while(i.hasNext()){
            Map.Entry ent = (Map.Entry)i.next();
                sb.append((String)ent.getValue() + "x  " + (String)ent.getKey() + "\n");
        }

        return "UID Invoice: " + UID + "\nCustomer: " + customer.toString() + "\nBuyed products: " + sb;
    }

}
