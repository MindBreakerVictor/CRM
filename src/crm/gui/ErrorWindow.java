package crm.gui;

import javax.swing.*;
import java.awt.*;

public class ErrorWindow {

    private JFrame frame;
    private JPanel mainPanel;
    private JLabel errorMessage;

    {
        frame = new JFrame("Error!");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - 400, screenSize.height / 2 - 200, 800, 200);
        frame.setContentPane(mainPanel);
    }

    public ErrorWindow(boolean exitOnClose) {
        if (exitOnClose)
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    public ErrorWindow(String message, boolean exitOnClose) {
        errorMessage.setText(message);

        if (exitOnClose)
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }
}
