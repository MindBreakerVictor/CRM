package crm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Robert Tanase on 01-May-17.
 */
public class AccountingGUI {

    JFrame frame = new JFrame ("Accounting");
    private JPanel menuPanel;
    private JButton homeButton;
    private JButton invoiceButton;
    private JButton accountingButton;
    private JButton depositButton;
    private JButton customersButton;
    private JButton invoicesButton;
    private JPanel mainPanel;
    private JPanel accountingPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JButton displayAllCustomersButton;
    private JButton addACustomerButton;
    private JButton displayIndividualsButton;
    private JButton displayCompaniesButton;
    private JTextArea customersList;

    public AccountingGUI() {
        frame.setBounds(100,100,800,500);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        addACustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.addCustomerGUI.displayWindow();
            }
        });
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.mainScreen.displayWindow();
                frame.setVisible(false); //you can't see me!
                try {
                    frame.dispose(); //Destroy the JFrame object
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        invoiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.invoiceScreen.displayWindow();
                frame.setVisible(false); //you can't see me!
                try {
                    frame.dispose(); //Destroy the JFrame object
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void displayWindow() {
        frame.setVisible(true);
    }
}
