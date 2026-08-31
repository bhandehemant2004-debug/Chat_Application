package Client;

import Framework.protocol.WebSocketFrameEncoder;
import Framework.protocol.WebSocketFrameParser;
import gui.MainFrame;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

public class ConnectionManager {
    public MainFrame mainFrame;
    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;
    public String host;
    public int port;
    public boolean is_connected = false;
    public boolean isWebSocketMode = false;

    public ConnectionManager() {
    }

    public ConnectionManager(String host, int port, MainFrame mainFrame) {
        this.host = host;
        this.port = port;
        this.mainFrame = mainFrame;
    }

    public void connect() {
        Thread thread = new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                is_connected = true;
                System.out.println("[ConnectionManager] Socket connected successfully to " + host + ":" + port);
                mainFrame.showScreen("REGISTER");

                while (is_connected && !socket.isClosed()) {
                    if (isWebSocketMode) {
                        // Read incoming RFC 6455 WebSocket frames from server
                        WebSocketFrameParser.Frame frame = WebSocketFrameParser.parseFrame(in);
                        if (frame.getOpcode() == WebSocketFrameParser.Opcode.TEXT) {
                            String msg = frame.getTextPayload();
                            System.out.println("[Client WS Rx] " + msg);
                            mainFrame.update(msg);
                        } else if (frame.getOpcode() == WebSocketFrameParser.Opcode.CLOSE) {
                            System.out.println("[Client WS Rx] WebSocket Close Frame Received.");
                            break;
                        }
                    } else {
                        // Read standard Java DataInputStream strings (writeUTF format)
                        String msg = in.readUTF();
                        System.out.println("[Client TCP Rx] " + msg);

                        if (msg.startsWith("Registration")) {
                            if (msg.equals("Registration_Success"))
                                mainFrame.onRegisterSuccess();
                            else
                                mainFrame.onRegisterFail();
                        } else if (msg.startsWith("Login")) {
                            if (msg.startsWith("Login_Success")) {
                                mainFrame.username = msg.split("\\|")[1];
                                mainFrame.onLoginSuccess();
                                // Upgrade connection to WebSocket mode
                                upgradeToWebSocket();
                            } else
                                mainFrame.onLoginFail();
                        } else {
                            mainFrame.update(msg);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ConnectionManager] Connection Error: " + e.getMessage());
                if (mainFrame != null) {
                    JOptionPane.showMessageDialog(mainFrame, "Disconnected from Chat Server");
                }
            }
        });
        thread.start();
    }

    public void upgradeToWebSocket() {
        try {
            String clientKey = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()).substring(0,
                    24);
            String handshakeMessage = "WS_UPGRADE|key=" + clientKey + "&user=" + mainFrame.username;

            out.writeUTF(handshakeMessage);
            out.flush();

            String response = in.readUTF();
            if (response.startsWith("WS_UPGRADE_SUCCESS")) {
                this.isWebSocketMode = true;
                System.out.println(
                        "[ConnectionManager] WebSocket Handshake successful! Client switched to RFC 6455 Mode for user: "
                                + mainFrame.username);
            } else {
                System.err.println("[ConnectionManager] WebSocket Upgrade rejected: " + response);
            }
        } catch (Exception e) {
            System.err.println("[ConnectionManager] WebSocket Upgrade Failed: " + e.getMessage());
        }
    }

    public void write(String msg) {
        try {
            if (isWebSocketMode) {
                WebSocketFrameEncoder.sendTextFrame(out, msg);
            } else {
                out.writeUTF(msg);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[ConnectionManager] Error writing message: " + e.getMessage());
        }
    }
}