package crm.frontend;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddCustomerGUI {

    JFrame frame = new JFrame ("Add Customer");
    private JPanel newCustomerPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField8;
    private JTextField textField7;
    private JButton addCustomerButton;

    public AddCustomerGUI() {

        frame.setBounds(100,100,800,500);
        frame.setContentPane(newCustomerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        addCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });
    }

    public void displayWindow() {
        frame.setVisible(true);
    }
}
