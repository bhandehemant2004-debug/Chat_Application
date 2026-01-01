

import java.net.*;
import java.util.Scanner;
import java.io.*;
public class Client {
    public static void main(String[]args){
        int port = 4000;
        String host = "localhost";
        String ip_address = "10.70.46.69";
        try{
            Socket socket = new Socket(host,port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            String str = input.readUTF();
            System.out.println(str);
            if(str.contains("username")){
                String username = scanner.nextLine();
                out.writeUTF(username);
                out.flush();
            }
            String s = input.readUTF() ;
            System.out.println(s);
            while(true){
                String message = scanner.nextLine();
                out.writeUTF(message);
                if (input.available() > 0) {
                    String msg = input.readUTF();
                    System.out.println("Received: " + msg);
                }
                if(message.equals("bye"))break;
            }
            socket.close();
        }
        catch(Exception e ){
            System.out.println(e);
        }
        
        
    }
}
