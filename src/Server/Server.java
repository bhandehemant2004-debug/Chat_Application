package Server;


import java.net.*;

import Thread.Reading_Writing;
import Framework.*;
public class Server{
    
  
    public static void main(String[]args){
            System.out.println("uploading a class annotations");
            Request_Mapping.main(args);
            try{
                int Port = 5000;
                ServerSocket server =
                new ServerSocket(5000, 50, InetAddress.getByName("0.0.0.0"));

                System.out.println("server is Started on port no :"+ Port);
                
                while(true){
                    Socket socket = server.accept();
                    System.out.println("one more connection available ");
                    Thread t = new Reading_Writing(socket);
                    t.start();
                } 
          
            }
            catch(Exception e){
                System.out.println(e);
            }
            
        
        }
    }
    
    
    
