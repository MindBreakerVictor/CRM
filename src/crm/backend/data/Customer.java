package crm.backend.data;

public abstract class Customer {

    private int id;
    private String deliveryAddress;
    private String contactNumber;

    public Customer() { }

    public Customer(int id, String deliveryAddress, String contactNumber) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.contactNumber = contactNumber;
    }

    public Customer(Customer customer) {
        id = customer.id;
        deliveryAddress = customer.deliveryAddress;
        contactNumber = customer.contactNumber;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getDeliveryAddress() { return deliveryAddress; }

    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getContactNumber() { return contactNumber; }

    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String toString() { return Integer.toString(id) + ", " + deliveryAddress + ", " + contactNumber; }
}
