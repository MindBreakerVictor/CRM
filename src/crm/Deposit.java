package crm;

import java.util.HashMap;

public class Deposit {

    private static HashMap<Product, Integer> products = new HashMap<>();

    public static boolean isInStock(Product p) { return products.get(p) != 0; }

    public static int getStock(Product p) {
        return products.get(p);
    }

    public static void addProduct(Product p, Integer quantity) {
        if (!products.containsKey(p)) {
            products.put(p, quantity);
        } else {
            products.put(p, products.get(p) + quantity);
        }
    }
}
