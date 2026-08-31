package Server;

import Controller.Chat_Controller;
import Framework.Request_Mapping;
import Framework.ioc.ApplicationContext;
import Services.Chat_Services;
import Thread.Reading_Writing;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Bootstrapping Custom Core Java Chat Server    ");
        System.out.println("=================================================");

        // Step 1: Boot up IoC Dependency Injection Container
        ApplicationContext context = new ApplicationContext(
                Chat_Services.class,
                Chat_Controller.class
        );

        // Step 2: Initialize Route Registry with IoC container
        Request_Mapping.initializeIoC(context);

        try {
            int port = 5000;
            ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("\n[Server Engine] Server is listening on port: " + port);

            while (true) {
                Socket socket = server.accept();
                System.out.println("[Server Engine] New TCP connection accepted from: " + socket.getRemoteSocketAddress());
                Thread t = new Reading_Writing(socket);
                t.start();
            }
        } catch (Exception e) {
            System.err.println("[Server Engine] Fatal Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
