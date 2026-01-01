import java.util.*;
import java.util.stream.IntStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
public class Server{
    public static LinkedList<String>linkedList ;

    public static void main(String[]args){
        
            try{
                int Port = 4000;
                ServerSocket server = new ServerSocket(Port);
                System.out.println("server is Started on port no :"+ Port);
                linkedList = new LinkedList<String>();
                while(true){
                    Socket socket = server.accept();
                    System.out.println("one more connection");
                    Thread thread = new Thread(new ClinetHandler(socket, linkedList));
                    thread.start();
                }        
            }
            catch(Exception e){
                System.out.println(e);
            }
            
        
        }
    }
    
    
    
