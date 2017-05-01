package crm;

import java.util.HashMap;
import java.util.Map;

public class Deposit {

    private static HashMap<Product, Integer> products;

    static {
        products = new HashMap<>();
    }

    public static boolean isInStock(Product p) { return products.get(p) != null; }

    public static int getStock(Product p) {
        return products.get(p);
    }

    public static void addProduct(Product p, Integer quantity) {
        if (!products.containsKey(p))
            products.put(p, quantity);
        else
            products.put(p, products.get(p) + quantity);
    }

    public static Product getProductByName(String name) {
        for (Map.Entry<Product, Integer> entry : products.entrySet())
            if (entry.getKey().getName().equals(name))
                return entry.getKey();

        return null;
    }

    public static void sellProduct(Product p, Integer quantity) {
        products.put(p, products.get(p) - quantity);
    }

    public void setStockToZero() {
        products.clear();
    }

    public HashMap<Product, Integer> getAllStock() {
        return products;
    }

    public void removeProduct(Product p) {
        products.remove(p);
    }
}
