package crm;

public class Product {

    private Double price;
    private String name;

    public Product() { }

    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInStock() {
        return Deposit.isInStock(this);
    }

    public int getStock() {
        return Deposit.getStock(this);
    }

    @Override
    public String toString() {
        return name + ", " + price.toString();
    }
}
