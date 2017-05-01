package crm.backend.data;

public class Individual extends Customer {

    private String firstName;
    private String lastName;

    public Individual() { }

    public Individual(String firstName, String lastName, int id, String deliveryAddress, String contactNumber) {
        super(id, deliveryAddress, contactNumber);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Individual(Individual individual) {
        super(individual.getId(), individual.getDeliveryAddress(), individual.getContactNumber());
        firstName = individual.firstName;
        lastName = individual.lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + firstName + ", " + lastName;
    }
}
