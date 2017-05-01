package crm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Deposit {

    private static  HashMap<Product, Integer> products = new HashMap<Product, Integer>();

    public static boolean isInStock(Product p) {
        if (products.containsKey(p)) {
            if (products.get(p) == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static int getStock(Product p) {
        return products.get(p);
    }

    public static void addProduct (Product p , Integer quantity) {
        if (!products.containsKey(p)) {
            products.put(p, quantity);
        } else {
            products.put(p, products.get(p) + quantity);
        }
    }

    public static Product getProductByName(String name) {
        Set entry = products.entrySet();
        Iterator i = entry.iterator();

        while(i.hasNext()){
            Map.Entry ent = (Map.Entry)i.next();
            if (((Product)ent.getKey()).getName().equals(name)) {
                return (Product)ent.getKey();
            }
        }
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
