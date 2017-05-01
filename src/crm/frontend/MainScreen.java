package crm.frontend;

import crm.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainScreen {

    JFrame frame = new JFrame ("MainScreen");
    private JPanel menuPanel;
    private JPanel homePanel;
    private JPanel mainPanel;
    private JButton homeButton;
    private JButton invoiceButton;
    private JButton accountingButton;
    private JButton depositButton;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JTextArea textArea4;
    private JTextArea textArea5;
    private JTextArea textArea6;
    private JLabel labelStatistics;
    private JLabel labelInformations;

    public MainScreen() {

        frame.setBounds(100,100,800,500);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO
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
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
               //TODO
            }
        });
    }

    public void displayWindow() {
        frame.setVisible(true);
    }

}

