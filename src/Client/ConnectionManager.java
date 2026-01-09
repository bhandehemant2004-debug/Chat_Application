package Client;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import gui.MainFrame;
public class ConnectionManager {
    MainFrame mainFrame;
    Socket socket;
    DataOutputStream out;
    DataInputStream in ;
    public String host ;
    public int port;
    public boolean is_connected=false;
    public ConnectionManager(){}
    public ConnectionManager( String host , int port , MainFrame mainFrame ){
        this.host = host;
        this.port = port ;
        this.mainFrame = mainFrame;
    }
    
    public void connect(){
        Thread thread = new Thread(()->{
            try{
                socket = new Socket(host,port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                is_connected= true;
                System.out.println(is_connected+" is connected properly");
                mainFrame.showScreen("REGISTER");
                while(is_connected){
                    System.out.println("waiting for the reply");
                    String msg = in.readUTF();
                    if(msg.startsWith("Registration")){
                        if(msg.equals("Registration_Success"))mainFrame.onRegisterSuccess();
                        else mainFrame.onRegisterFail();
                        
                    }
                    else if(msg.startsWith("Login")){
                        if(msg.startsWith("Login_Success")){
                            mainFrame.username=msg.split("\\|")[1];
                            mainFrame.onLoginSuccess();}
                        else mainFrame.onLoginFail();
                    }
                    else {
                        System.out.println(msg);
                        mainFrame.update(msg);
                    }
                    Thread.sleep(100);
                }
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(mainFrame, "Unable to connect");
                mainFrame.dispose();
                System.out.println("error int the connect manager class"+e);
            }
            

        });
        thread.start();
        
    }
    public void write(String msg){

        try{
            out.writeUTF(msg);
            out.flush();
        }
        catch(Exception e){
            System.out.println("Error while writing the msg");
        }
    }
}