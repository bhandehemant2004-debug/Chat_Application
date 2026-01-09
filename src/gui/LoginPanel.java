package gui;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    
    public LoginPanel(MainFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

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
        add(loginBtn, gbc);

        gbc.gridy++;
        add(registerBtn, gbc);

        loginBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword()); 
            String msg = "POST|Generate_token|"+"username="+user+"&"+"password="+pass;
            System.out.println("sending request : "+msg);
            frame.get_connection().write(msg);
        });

        registerBtn.addActionListener(e -> frame.showScreen("REGISTER"));
    }
    
    
}
