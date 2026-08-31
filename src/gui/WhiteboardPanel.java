package gui;

import Client.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class WhiteboardPanel extends JPanel {

    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int prevX, prevY;
    private Color currentColor = Color.BLACK;
    private int strokeSize = 3;
    private ConnectionManager connectionManager;

    private String currentRoomId = null;
    private JLabel roomStatusLabel;

    public WhiteboardPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        setLayout(new BorderLayout());

        // TOP TOOLBAR: Room Controls, Colors, Clear
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton createRoomBtn = new JButton("➕ Create Room");
        JButton joinRoomBtn = new JButton("🔑 Join Room");
        JButton leaveRoomBtn = new JButton("🚪 Leave Room");

        roomStatusLabel = new JLabel(" Status: Standalone Canvas ");
        roomStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));

        createRoomBtn.addActionListener(e -> handleCreateRoom());
        joinRoomBtn.addActionListener(e -> handleJoinRoom());
        leaveRoomBtn.addActionListener(e -> handleLeaveRoom());

        JButton blackBtn = createColorButton(Color.BLACK);
        JButton redBtn = createColorButton(Color.RED);
        JButton blueBtn = createColorButton(Color.BLUE);
        JButton greenBtn = createColorButton(Color.GREEN);
        JButton orangeBtn = createColorButton(Color.ORANGE);
        JButton magentaBtn = createColorButton(Color.MAGENTA);
        JButton eraserBtn = new JButton("🧹 Eraser");

        eraserBtn.addActionListener(e -> currentColor = Color.WHITE);

        JButton clearBtn = new JButton("❌ Clear Canvas");
        clearBtn.addActionListener(e -> {
            clearCanvas();
            if (this.connectionManager != null && this.connectionManager.isWebSocketMode) {
                if (currentRoomId != null) {
                    this.connectionManager.write("CANVAS_CLEAR|" + currentRoomId);
                } else {
                    this.connectionManager.write("DRAW_CLEAR");
                }
            }
        });

        toolBar.add(createRoomBtn);
        toolBar.add(joinRoomBtn);
        toolBar.add(leaveRoomBtn);
        toolBar.addSeparator();
        toolBar.add(new JLabel(" Colors: "));
        toolBar.add(blackBtn);
        toolBar.add(redBtn);
        toolBar.add(blueBtn);
        toolBar.add(greenBtn);
        toolBar.add(orangeBtn);
        toolBar.add(magentaBtn);
        toolBar.addSeparator();
        toolBar.add(eraserBtn);
        toolBar.addSeparator();
        toolBar.add(clearBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(toolBar, BorderLayout.CENTER);
        topContainer.add(roomStatusLabel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // CANVAS DRAWING AREA
        JPanel drawArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (canvasImage != null) {
                    g.drawImage(canvasImage, 0, 0, null);
                }
            }
        };
        drawArea.setBackground(Color.WHITE);

        drawArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }
        });

        drawArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getX();
                int currentY = e.getY();

                drawSegment(prevX, prevY, currentX, currentY, currentColor, strokeSize);

                if (connectionManager != null && connectionManager.isWebSocketMode) {
                    String colorHex = String.format("#%06X", (0xFFFFFF & currentColor.getRGB()));
                    if (currentRoomId != null) {
                        String drawData = "CANVAS_DRAW|" + currentRoomId + "|" + prevX + "|" + prevY + "|" + currentX + "|" + currentY + "|" + colorHex + "|" + strokeSize;
                        connectionManager.write(drawData);
                    } else {
                        String drawData = "DRAW|" + prevX + "|" + prevY + "|" + currentX + "|" + currentY + "|" + colorHex + "|" + strokeSize;
                        connectionManager.write(drawData);
                    }
                }

                prevX = currentX;
                prevY = currentY;
                drawArea.repaint();
            }
        });

        add(drawArea, BorderLayout.CENTER);
    }

    private void handleCreateRoom() {
        JTextField roomField = new JTextField();
        JPasswordField pinField = new JPasswordField();
        Object[] message = {
                "Canvas Room Name:", roomField,
                "Set Security PIN:", pinField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Create Canvas Room", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String roomId = roomField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            if (!roomId.isEmpty() && !pin.isEmpty()) {
                if (connectionManager != null && connectionManager.isWebSocketMode) {
                    connectionManager.write("CANVAS_CREATE|" + roomId + "|" + pin);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Room name and PIN cannot be empty!");
            }
        }
    }

    private void handleJoinRoom() {
        JTextField roomField = new JTextField();
        JPasswordField pinField = new JPasswordField();
        Object[] message = {
                "Canvas Room Name:", roomField,
                "Enter Security PIN:", pinField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Join Canvas Room", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String roomId = roomField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            if (!roomId.isEmpty() && !pin.isEmpty()) {
                if (connectionManager != null && connectionManager.isWebSocketMode) {
                    connectionManager.write("CANVAS_JOIN|" + roomId + "|" + pin);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Room name and PIN cannot be empty!");
            }
        }
    }

    private void handleLeaveRoom() {
        if (currentRoomId != null) {
            if (connectionManager != null && connectionManager.isWebSocketMode) {
                connectionManager.write("CANVAS_LEAVE|" + currentRoomId);
            }
            onRoomLeft();
        }
    }

    public void onRoomJoined(String roomId) {
        this.currentRoomId = roomId;
        clearCanvas();
        roomStatusLabel.setText(" Status: 🟢 Connected to Canvas Room '" + roomId + "' ");
        roomStatusLabel.setForeground(new Color(0, 120, 0));
    }

    public void onRoomLeft() {
        this.currentRoomId = null;
        clearCanvas();
        roomStatusLabel.setText(" Status: Standalone Canvas ");
        roomStatusLabel.setForeground(Color.BLACK);
    }

    private JButton createColorButton(Color c) {
        JButton btn = new JButton();
        btn.setBackground(c);
        btn.setPreferredSize(new Dimension(24, 24));
        btn.addActionListener(e -> currentColor = c);
        return btn;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initCanvasImage();
    }

    private void initCanvasImage() {
        if (canvasImage == null) {
            int w = Math.max(800, getWidth());
            int h = Math.max(600, getHeight());
            canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g2d = canvasImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            clearCanvas();
        }
    }

    public void clearCanvas() {
        if (g2d != null) {
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
            g2d.setComposite(AlphaComposite.SrcOver);
            repaint();
        }
    }

    public void drawSegment(int x1, int y1, int x2, int y2, Color color, int stroke) {
        if (canvasImage == null) {
            initCanvasImage();
        }
        if (g2d != null) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
            repaint();
        }
    }
}
