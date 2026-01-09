package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class postman {

    public static void main(String[] args) {

        String host = "10.146.78.202"; // server IP
        int port = 5000;

        try {
            Socket socket = new Socket(host, port);
            System.out.println("Connected to server " + host + ":" + port);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Thread for reading from server
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String response = in.readUTF(); // blocking call
                        System.out.println("\nServer: " + response);
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server (read thread).");
                }
            });

            // Thread for writing to server
            Thread writeThread = new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        System.out.print("You: ");
                        String request = scanner.nextLine();
                        out.writeUTF(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server (write thread).");
                }
            });

            // Start both threads
            readThread.start();
            writeThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}