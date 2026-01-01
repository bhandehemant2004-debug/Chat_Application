import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
public class ClinetHandler implements Runnable{
    private Socket socket;
    boolean connect = false;
    String Username;
    LinkedList<String>linkedList;
    public ClinetHandler(Socket socket,LinkedList<String>linkedlist){
        this.socket = socket;
        this.linkedList= linkedlist;
        connect = true;
    }
    public void run(){
        
        try{
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello you are connected to port 4000 : write your username");
            //out.flush();
            Username = input.readUTF();//wait for the username
            System.out.print(Username+"is connected");
            out.writeUTF(" Able to communicate with the group ");
            while(true){
                String str = input.readUTF();
                linkedList.add(Username+": "+str);
                System.out.println(Username+": "+str);
                if(str.equals("bye")){
                    out.writeUTF("bye");
                    //out.flush();
                    break;
                }
            }
            socket.close();
        }
        catch(Exception e){
            System.out.println(e);

        }
       
    }
}