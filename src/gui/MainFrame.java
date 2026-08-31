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
    public WhiteboardPanel whiteboardPanel;
    public IdentityManager identityManager;
    public String username;

    public MainFrame() {
        String host = "";
        int port = 0;
        try {
            identityManager = new IdentityManager();
            host = identityManager.getServerIP();
            port = identityManager.getPort();
        } catch (Exception exp) {
            System.out.println("not able to deport port numbe");
            exp.printStackTrace();
        }
        System.out.println("host " + host + " port" + port);

        connectionManager = new ConnectionManager(host, port, this);
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        chatPanel = new ChatPanel(this);
        whiteboardPanel = new WhiteboardPanel(connectionManager);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("💬 Chat & Messages", chatPanel);
        tabbedPane.addTab("🎨 Collaborative Canvas", whiteboardPanel);

        container.add(new ConnectingPanel(), "Connecting");
        container.add(new LoginPanel(this), "LOGIN");
        container.add(new RegisterPanel(this), "REGISTER");
        container.add(tabbedPane, "CHAT");

        add(container);
        setTitle("SecureChat & Collaborative Canvas");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        showScreen("Connecting");
        connectionManager.connect();
    }

    public void showScreen(String name) {
        cardLayout.show(container, name);
    }

    public ConnectionManager get_connection() {
        return connectionManager;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }

    public void onRegisterSuccess() {
        JOptionPane.showMessageDialog(this, "Registration Successful!");
        this.showScreen("LOGIN");
    }

    public void onRegisterFail() {
        JOptionPane.showMessageDialog(this, "Registration Failed!");
        this.showScreen("REGISTER");
    }

    public void onLoginSuccess() {
        JOptionPane.showMessageDialog(this, "Login Successful!");
        this.showScreen("CHAT");
    }

    public void onLoginFail() {
        JOptionPane.showMessageDialog(this, "Login Failed!");
        this.showScreen("LOGIN");
    }

    public void update(String sms) {
        if (sms.startsWith("CANVAS_JOIN_SUCCESS|")) {
            String roomId = sms.split("\\|")[1];
            if (whiteboardPanel != null) {
                whiteboardPanel.onRoomJoined(roomId);
            }
            JOptionPane.showMessageDialog(this, "Joined Canvas Room: " + roomId);
        } else if (sms.startsWith("CANVAS_ERROR|")) {
            String err = sms.split("\\|")[1];
            JOptionPane.showMessageDialog(this, err, "Canvas Room Error", JOptionPane.ERROR_MESSAGE);
        } else if (sms.startsWith("CANVAS_DRAW|") || sms.startsWith("DRAW|")) {
            String[] parts = sms.split("\\|");
            int offset = sms.startsWith("CANVAS_DRAW|") ? 2 : 1;
            if (parts.length >= offset + 6) {
                int x1 = Integer.parseInt(parts[offset]);
                int y1 = Integer.parseInt(parts[offset + 1]);
                int x2 = Integer.parseInt(parts[offset + 2]);
                int y2 = Integer.parseInt(parts[offset + 3]);
                Color c = Color.decode(parts[offset + 4]);
                int stroke = Integer.parseInt(parts[offset + 5]);
                if (whiteboardPanel != null) {
                    whiteboardPanel.drawSegment(x1, y1, x2, y2, c, stroke);
                }
            }
        } else if (sms.startsWith("CANVAS_CLEAR|") || sms.equals("DRAW_CLEAR")) {
            if (whiteboardPanel != null) {
                whiteboardPanel.clearCanvas();
            }
        } else if (chatPanel != null) {
            chatPanel.appendMessage(sms);
        }
    }
}
