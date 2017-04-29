package crm;

public class Company extends Customer {

    private String name;
    private String fiscalCode;
    private String bankAccount;
    private String headquartersAddress;

    public Company() { }

    public Company(String name, String fiscalCode, String bankAccount, String headquartersAddress,
                   String deliveryAddress, String contactNumber) {
        super(deliveryAddress, contactNumber);
        this.name = name;
        this.fiscalCode = fiscalCode;
        this.bankAccount = bankAccount;
        this.headquartersAddress = headquartersAddress;
    }

    public Company(Company company) {
        super(company.getDeliveryAddress(), company.getContactNumber());
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
}
