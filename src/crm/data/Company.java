package crm.data;

public class Company extends Customer {

    private String name;
    private String fiscalCode;
    private String bankAccount;
    private String headquartersAddress;

    public Company() { }

    public Company(String name, String fiscalCode, String bankAccount, String headquartersAddress,
                   String deliveryAddress, String contactNumber) {
        this(name, fiscalCode, bankAccount, headquartersAddress, 0, deliveryAddress, contactNumber);
    }

    public Company(String name, String fiscalCode, String bankAccount, String headquartersAddress,
                   int id, String deliveryAddress, String contactNumber) {
        super(id, deliveryAddress, contactNumber);
        this.name = name;
        this.fiscalCode = fiscalCode;
        this.bankAccount = bankAccount;
        this.headquartersAddress = headquartersAddress;
    }

    public Company(Company company) {
        super(company.getId(), company.getDeliveryAddress(), company.getContactNumber());
        name = company.name;
        fiscalCode = company.fiscalCode;
        bankAccount = company.bankAccount;
        headquartersAddress = company.headquartersAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getHeadquartersAddress() {
        return headquartersAddress;
    }

    public void setHeadquartersAddress(String headquartersAddress) {
        this.headquartersAddress = headquartersAddress;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + name + ", " + fiscalCode + ", " + bankAccount + ", " + headquartersAddress;
    }
}
