package gui;

import javax.swing.*;
import java.awt.*;
import Client.ConnectionManager;
import Security.IdentityManager;
public class MainFrame extends JFrame {
    private static ConnectionManager connectionManager;
    private CardLayout cardLayout;
    private JPanel container;
    public ChatPanel chatPanel;
    public IdentityManager identityManager ;
    public String username;
    public MainFrame() {
        String host ="";
        int port=0 ;
        try{
            identityManager = new IdentityManager();
            host = identityManager.getServerIP();
            port = identityManager.getPort();
        }
        catch(Exception exp){
            System.out.println("not able to deport port numbe");
            exp.printStackTrace();
        }
        System.out.println("host "+host+" port"+port);
        
        connectionManager = new ConnectionManager(host, port,this);
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        chatPanel = new ChatPanel(this);
        container.add(new ConnectingPanel(),"Connecting");
        container.add(new LoginPanel(this), "LOGIN");
        container.add(new RegisterPanel(this), "REGISTER");
        container.add(chatPanel, "CHAT");
        
        add(container);
        setTitle("SecureChat");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        showScreen("Connecting");
        connectionManager.connect();     
    }
    
    public  void showScreen(String name) {
        cardLayout.show(container, name);
    }
    public ConnectionManager get_connection(){
        return connectionManager;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
    public  void onRegisterSuccess(){
        JOptionPane.showMessageDialog(this, "Registration Successful!");
        this.showScreen("LOGIN");
    }
    public  void onRegisterFail(){
        JOptionPane.showMessageDialog(this, "Registration Successful!");
        this.showScreen("REGISTER");
    }
    public void onLoginSuccess(){
        JOptionPane.showMessageDialog(this, "Login Successful!");
        this.showScreen("CHAT");
    }
    public void onLoginFail(){
        JOptionPane.showMessageDialog(this, "Login unSuccessful!");
        this.showScreen("LOGIN");
    }
    public void update(String sms) {
        if (chatPanel != null) {
            chatPanel.appendMessage(sms);
        }
    }
    
}

