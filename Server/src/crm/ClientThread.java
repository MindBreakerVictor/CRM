package crm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket socket;
    private BufferedReader socketIn;
    private DataOutputStream socketOut;

    ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOut = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        String json;

        while (true) {
            try {
                json = readAll(socketIn);
                // TODO: handle requests.
            } catch (IOException exception) {
                System.out.println("IOException: " + exception.getMessage());
                exception.printStackTrace();
                closeSocket();
            }
        }
    }

    private void closeSocket() {
        try {
            socketIn.close();
            socketOut.close();
            socket.close();
        } catch (IOException exception) {
            System.out.println("IOException: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static String readAll(BufferedReader socketIn) throws IOException {
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = socketIn.readLine()) != null)
            stringBuilder.append(line).append("\n");

        return stringBuilder.toString();
    }
}
