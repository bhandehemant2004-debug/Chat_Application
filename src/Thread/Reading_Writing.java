package Thread;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import Framework.*;

public class Reading_Writing extends Thread {
    public Socket socket;
    public DataInputStream in ;
    public DataOutputStream out;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    String username;

    public Reading_Writing(Socket socket){
        this.socket = socket;
        try{
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            // Writer thread
        Thread writer = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    String msg = queue.take(); // waits safely
                    out.writeUTF(msg);
                    out.flush();
                }
            } catch (Exception e) {
                try { socket.close(); } catch (Exception ignored) {}
            }
        });
        writer.start();
        }
        catch(Exception e){
            System.out.println("cannot form a socket connection"+e);
        }
    }
    // Add message to queue (non-blocking for server)
    public void send(String msg) {
        queue.offer(msg);
    }
    
    public void run(){
        try{
            while(true){
            System.out.println("welcome to the thread");
            String raw = in.readUTF();
            System.out.println("received request :"+raw);
            Request request = RequestParser.parse(raw,this);
            System.out.println("Received request : "+ request);
            String routeKey = request.method + "|" + request.path;
            System.out.println("routekey for the request : "+routeKey);
            Method method = Request_Mapping.getMethod(routeKey);
            System.out.print(method);
            
            
            MethodInvoker.invoke(method, request);
            //System.out.println("sending response: "+ response.toString());
            
            //send(response.toString());
            }
        }
        catch(Exception e){}
    }

    
}
