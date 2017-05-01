package crm.frontend;

import crm.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InvoiceScreen {

    JFrame frame = new JFrame ("Add Invoice");
    private JPanel menuPanel;
    private JButton homeButton;
    private JButton invoiceButton;
    private JButton accountingButton;
    private JButton depositButton;
    private JPanel mainPanel;
    private JList customersList;
    private JLabel labelCustomer;
    private JPanel invoicePanel;
    private JList productsList;
    private JTextField quantity;
    private JLabel priceLabel;
    private JButton createInvoiceButton;
    private JButton addProductButton;

    public InvoiceScreen() {

        frame.setBounds(100,100,800,500);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

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
        accountingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.accountingGUI.displayWindow();
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
