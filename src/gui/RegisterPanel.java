package gui;

import javax.swing.*;
import java.awt.*;
//import src.Security.*;
public class RegisterPanel extends JPanel {
    
    public RegisterPanel(MainFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel title = new JLabel("Register");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);

        JButton registerBtn = new JButton("Create Account");
        JButton backBtn = new JButton("Back");

        gbc.gridy = 0;
        add(title, gbc);

        gbc.gridy++;
        add(new JLabel("Username"), gbc);
        gbc.gridy++;
        add(username, gbc);

        gbc.gridy++;
        add(new JLabel("Password"), gbc);
        gbc.gridy++;
        add(password, gbc);

        gbc.gridy++;
        add(registerBtn, gbc);

        gbc.gridy++;
        add(backBtn, gbc);
        registerBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword()); 
            try{
                
            }
            catch(Exception exception){
                System.out.println("exception e : " +exception);
            }
            String msg = "POST|Registration|"+"username="+user+"&"+"password="+pass;
            System.out.println("request to the server : "+ msg);
            frame.get_connection().write(msg);
        });
        backBtn.addActionListener(e -> frame.showScreen("LOGIN"));
    }
}
