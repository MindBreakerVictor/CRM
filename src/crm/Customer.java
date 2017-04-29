package crm;

public abstract class Customer {

    private String deliveryAddress;
    private String contactNumber;

    public Customer() { }

    public Customer(String deliveryAddress, String contactNumber) {
        this.deliveryAddress = deliveryAddress;
        this.contactNumber = contactNumber;
    }

    public Customer(Customer customer) {
        deliveryAddress = customer.deliveryAddress;
        contactNumber = customer.contactNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
