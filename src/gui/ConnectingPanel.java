package gui;

import javax.swing.*;
import java.awt.*;

public class ConnectingPanel extends JPanel {

    private JLabel statusLabel;

    public ConnectingPanel() {
        setLayout(new GridBagLayout());
        statusLabel = new JLabel("Connecting to server...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(statusLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}

