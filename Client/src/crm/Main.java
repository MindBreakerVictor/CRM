package crm;

import crm.gui.ErrorWindow;
import crm.gui.MainWindow;

import java.io.IOException;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            new MainWindow(new Socket("lcoalhost", 27015));
        } catch (IOException exception) {
            new ErrorWindow("IOException: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
