package crm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            Socket socket = null;
            ServerSocket serverSocket = new ServerSocket(27015);

            while (true) {
                // Todo: add interface and stop event.
                try {
                    socket = serverSocket.accept();
                } catch (IOException exception) {
                    System.out.println("IOException: " + exception.getMessage());
                    exception.printStackTrace();
                }

                new ClientThread(socket).start();
            }
        } catch (IOException exception) {
            System.out.println("IOException: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
