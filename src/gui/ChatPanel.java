package gui;

import Security.IdentityManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ChatPanel extends JPanel {

    private DefaultListModel<String> userListModel;
    private JList<String> usersList;
    private JPanel chatDisplayContainer;
    private CardLayout chatCardLayout;
    private Map<String, JTextArea> chatAreasMap = new HashMap<>();

    private JTextField messageField;
    private JLabel recipientLabel;
    private JCheckBox secureModeBox;
    private String selectedRecipient = "Broadcast (Everyone)";
    private IdentityManager identityManager;
    private MainFrame mainframe;

    public ChatPanel(MainFrame mainframe) {
        this.mainframe = mainframe;
        try {
            identityManager = new IdentityManager();
        } catch (Exception e) {
            System.err.println("[ChatPanel] Could not initialize IdentityManager: " + e.getMessage());
        }

        setLayout(new BorderLayout());

        // LEFT: Active users list
        userListModel = new DefaultListModel<>();
        userListModel.addElement("Broadcast (Everyone)");
        usersList = new JList<>(userListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setSelectedIndex(0);

        JScrollPane usersScroll = new JScrollPane(usersList);
        usersScroll.setPreferredSize(new Dimension(200, 0));
        usersScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));

        // RIGHT: Dedicated Chat Card Panels (per user channel)
        chatCardLayout = new CardLayout();
        chatDisplayContainer = new JPanel(chatCardLayout);

        // Add main public broadcast room
        getOrCreateChatArea("Broadcast (Everyone)");

        usersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String val = usersList.getSelectedValue();
                if (val != null) {
                    selectedRecipient = val;
                    recipientLabel.setText("Chatting with: " + selectedRecipient);
                    getOrCreateChatArea(selectedRecipient);
                    chatCardLayout.show(chatDisplayContainer, selectedRecipient);
                }
            }
        });

        // MESSAGE BAR
        JPanel inputPanel = new JPanel(new BorderLayout());
        recipientLabel = new JLabel("Chatting with: " + selectedRecipient + "  ");
        recipientLabel.setFont(new Font("Arial", Font.BOLD, 12));
        messageField = new JTextField();
        JButton attachBtn = new JButton("📎 File");
        JButton sendBtn = new JButton("Send");
        secureModeBox = new JCheckBox("🔒 Secure Mode");
        secureModeBox.setFont(new Font("Arial", Font.BOLD, 11));

        JPanel leftInput = new JPanel(new BorderLayout());
        leftInput.add(recipientLabel, BorderLayout.WEST);
        leftInput.add(messageField, BorderLayout.CENTER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightControls.add(attachBtn);
        rightControls.add(secureModeBox);
        rightControls.add(sendBtn);

        inputPanel.add(leftInput, BorderLayout.CENTER);
        inputPanel.add(rightControls, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatDisplayContainer, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        add(usersScroll, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        attachBtn.addActionListener(e -> handleFileAttachment());

        sendBtn.addActionListener(e -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                if (secureModeBox.isSelected() && identityManager != null) {
                    try {
                        String signature = identityManager.signMessage(msg);
                        String pubKey = identityManager.getPublicKey();
                        String secureMsg = "[RSA 🔒] " + msg;

                        if (mainframe.get_connection().isWebSocketMode) {
                            if (!"Broadcast (Everyone)".equals(selectedRecipient)) {
                                secureMsg = "/pm " + selectedRecipient + " " + secureMsg;
                            }
                            mainframe.get_connection().write(secureMsg);
                        } else {
                            String httpMsg = "POST|secureChat|username=" + mainframe.username +
                                    "&message=" + java.net.URLEncoder.encode(msg, "UTF-8") +
                                    "&signature=" + java.net.URLEncoder.encode(signature, "UTF-8") +
                                    "&publicKey=" + java.net.URLEncoder.encode(pubKey, "UTF-8");
                            mainframe.get_connection().write(httpMsg);
                        }
                    } catch (Exception ex) {
                        System.err.println("[ChatPanel] Error signing message: " + ex.getMessage());
                    }
                } else {
                    if (mainframe.get_connection().isWebSocketMode) {
                        if (!"Broadcast (Everyone)".equals(selectedRecipient)) {
                            msg = "/pm " + selectedRecipient + " " + msg;
                        }
                        mainframe.get_connection().write(msg);
                    } else {
                        String httpMsg = "POST|send_msg|username=" + mainframe.username + "&message=" + msg;
                        mainframe.get_connection().write(httpMsg);
                    }
                }
                messageField.setText("");
            }
        });
    }

    private void handleFileAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                String fileName = selectedFile.getName();

                String fileMsg = "FILE_SEND|" + selectedRecipient + "|" + fileName + "|" + base64Content;
                if (mainframe.get_connection().isWebSocketMode) {
                    mainframe.get_connection().write(fileMsg);
                    
                    // Display sent confirmation in current chat tab
                    JTextArea targetArea = getOrCreateChatArea(selectedRecipient);
                    targetArea.append("[Me]: 📎 Sent file '" + fileName + "' (" + fileBytes.length + " bytes)\n");
                    targetArea.setCaretPosition(targetArea.getDocument().getLength());
                } else {
                    JOptionPane.showMessageDialog(this, "Connect to WebSocket session to send files!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTextArea getOrCreateChatArea(String channelName) {
        if (!chatAreasMap.containsKey(channelName)) {
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 14));
            JScrollPane scroll = new JScrollPane(area);
            chatAreasMap.put(channelName, area);
            chatDisplayContainer.add(scroll, channelName);
        }
        return chatAreasMap.get(channelName);
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("USER_LIST_UPDATE|")) {
                updateUserList(msg.substring("USER_LIST_UPDATE|".length()));
            } else if (msg.startsWith("[PM from ")) {
                int endSender = msg.indexOf("]:");
                if (endSender != -1) {
                    String sender = msg.substring("[PM from ".length(), endSender).trim();
                    String content = msg.substring(endSender + 2).trim();
                    JTextArea targetArea = getOrCreateChatArea(sender);
                    targetArea.append("[" + sender + "]: " + content + "\n");
                    targetArea.setCaretPosition(targetArea.getDocument().getLength());
                }
            } else if (msg.startsWith("[PM to ")) {
                int endRecipient = msg.indexOf("]:");
                if (endRecipient != -1) {
                    String recipient = msg.substring("[PM to ".length(), endRecipient).trim();
                    String content = msg.substring(endRecipient + 2).trim();
                    JTextArea targetArea = getOrCreateChatArea(recipient);
                    targetArea.append("[Me]: " + content + "\n");
                    targetArea.setCaretPosition(targetArea.getDocument().getLength());
                }
            } else if (msg.startsWith("[FILE_RECEIVED|")) {
                // Example: [FILE_RECEIVED|sender|fileName|savedPath]
                String[] parts = msg.split("\\|", 4);
                if (parts.length >= 4) {
                    String sender = parts[1];
                    String fileName = parts[2];
                    String path = parts[3];
                    JTextArea targetArea = getOrCreateChatArea(sender);
                    targetArea.append("[" + sender + "]: 📁 Shared file '" + fileName + "' (Saved at: " + path + ")\n");
                    targetArea.setCaretPosition(targetArea.getDocument().getLength());
                }
            } else {
                JTextArea publicArea = getOrCreateChatArea("Broadcast (Everyone)");
                publicArea.append(msg + "\n");
                publicArea.setCaretPosition(publicArea.getDocument().getLength());
            }
        });
    }

    private void updateUserList(String csvUsers) {
        userListModel.clear();
        userListModel.addElement("Broadcast (Everyone)");
        if (!csvUsers.isEmpty()) {
            String[] users = csvUsers.split(",");
            for (String u : users) {
                if (!u.trim().isEmpty() && !u.trim().equals(mainframe.username)) {
                    userListModel.addElement(u.trim());
                }
            }
        }
        usersList.setSelectedValue(selectedRecipient, true);
        if (usersList.getSelectedIndex() == -1) {
            usersList.setSelectedIndex(0);
        }
    }
}
