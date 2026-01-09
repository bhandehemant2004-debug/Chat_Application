package gui;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {

    private JList<String> usersList;
    private JTextArea chatArea;
    private JTextField messageField;

    public ChatPanel(MainFrame mainframe) {
        setLayout(new BorderLayout());

        // LEFT: users list
        usersList = new JList<>(new String[]{
                "Rahul", "Amit", "Neha"
        });
        JScrollPane usersScroll = new JScrollPane(usersList);
        usersScroll.setPreferredSize(new Dimension(200, 0));

        // RIGHT: chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // MESSAGE BAR
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendBtn = new JButton("Send");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        add(usersScroll, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        sendBtn.addActionListener(e -> {
            String msg = messageField.getText();
            if (!msg.isEmpty()) {
                chatArea.append("Me: " + msg + "\n");
                messageField.setText("");
                msg = "POST|send_msg|username="+mainframe.username+"&message="+msg;
                System.out.println("request send : "+msg);
                mainframe.get_connection().write(msg);
            }
        });
    }
    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
}

