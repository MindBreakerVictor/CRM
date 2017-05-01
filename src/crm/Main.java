package crm;

import crm.backend.data.*;
import crm.backend.management.Accounting;
import crm.backend.management.Deposit;
import crm.frontend.AccountingGUI;
import crm.frontend.AddCustomerGUI;
import crm.frontend.InvoiceScreen;
import crm.frontend.MainScreen;

import java.util.Scanner;

public class Main {

    public static MainScreen mainScreen = new MainScreen();
    public static InvoiceScreen invoiceScreen = new InvoiceScreen();
    public static AddCustomerGUI addCustomerGUI = new AddCustomerGUI();
    public  static AccountingGUI accountingGUI = new AccountingGUI();
    private static int idCustomer = 1;

    public static void main(String[] args) {

       /*
        setDeposit();
        addCustomers();
        createInvoice();
         */
    }

    public static void addCustomers() {
        String cont;
        Scanner sc = new Scanner(System.in);

        while (true) {
            Customer customer = addOneCustomer();
            Accounting.addCustomer(customer);
            System.out.println("Do you want to add another customer?  Y/N");
            cont = sc.next();
            if(!cont.equals("Y"))
                break;
        }
    }

    public static Customer addOneCustomer() {
        Customer customer;
        int type;
        String address;
        String phone;
        String firstName;
        String lastName;
        String name;
        String fiscalCode;
        String bankAccount;
        String headquartersAddress;

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.print("Enter  1 - Individual Customer\n       2 - Company Customer   ");
            type = sc.nextInt();

            if (type == 1) {
                System.out.print("First Name: ");
                firstName = sc.next();
                System.out.print("Last Name: ");
                lastName = sc.next();
                System.out.print("Address: ");
                address = sc.next();
                System.out.print("Phone: ");
                phone = sc.next();

                customer = new Individual(firstName, lastName, idCustomer, address, phone);
                idCustomer++;
                break;
            }

            if (type == 2) {
                System.out.print("Company Name: ");
                name = sc.next();
                System.out.print("Fiscal Code: ");
                fiscalCode = sc.next();
                System.out.print("Bank Account: ");
                bankAccount = sc.next();
                System.out.print("Headquarters Address: ");
                headquartersAddress = sc.next();
                System.out.print("Address: ");
                address = sc.next();
                System.out.print("Phone: ");
                phone = sc.next();

                customer = new Company(name, fiscalCode, bankAccount, headquartersAddress, idCustomer, address, phone);
                idCustomer++;
                break;
            }
            System.out.println("Please choose a valid option");

        }

        return customer;
    }

    public static void createInvoice() {
        Invoice invoice;
        String name;
        Integer quantity;
        String cont, newC;
        int id;

        Scanner sc = new Scanner(System.in);

        System.out.println("Do you want to add a new customer?  Y/N");
        newC = sc.next();
        if (newC.equals("Y")) {
            Customer newCustomer = addOneCustomer();
            Accounting.addCustomer(newCustomer);
            invoice = new Invoice(newCustomer);
        } else {
            System.out.println(Accounting.displayCustomers());
            System.out.println("Choose the customer by ID");
            id = sc.nextInt();
            Customer customer = Accounting.getCustomerById(id);
            invoice = new Invoice(customer);
        }

        while(true) {
            Product p;
            while(true) {
                System.out.println("Enter the name of the product ");
                name = sc.next();
                p = Deposit.getProductByName(name);
                if (p != null && p.isInStock()) {
                    while (true) {
                        System.out.print("Set the quantity [max. " + p.getStock() + "]   ");
                        quantity = sc.nextInt();
                        if (quantity <= p.getStock())
                            break;
                    }
                    break;
                }
                System.out.println("The desired product doesn't exist. Please try again. ");
            }
            invoice.addProduct(p, quantity);
            System.out.println("Do you want to set another product? Y\\N");
            cont = sc.next();
            if(!cont.equals("Y"))
                break;
        }
        Accounting.addInvoice(invoice);

    }

    public static void setDeposit() {

        String name;
        Double price;
        Integer quantity;
        String cont;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter product's name ");
        name = sc.next();
        System.out.println("Set product's price ");
        price = sc.nextDouble();
        System.out.println("Set the quantity");
        quantity = sc.nextInt();

        while(true) {
            Product p = new Product(name, price);
            Deposit.addProduct(p, quantity);
            System.out.println("Do you want to set another product? Y\\N");
            cont = sc.next();
            if(!cont.equals("Y"))
                break;
        }
    }
}
