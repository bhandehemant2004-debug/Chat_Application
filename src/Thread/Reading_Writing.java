package Thread;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Framework.*;
import Framework.canvas.CanvasRoomManager;
import Framework.protocol.WebSocketFrameEncoder;
import Framework.protocol.WebSocketFrameParser;
import Framework.protocol.WebSocketHandshake;
import Framework.session.WebSocketSessionRegistry;

public class Reading_Writing extends Thread {
    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    public String username;
    public boolean isWebSocketSession = false;

    public Reading_Writing(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Writer thread for standard TCP string queue
            Thread writer = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        String msg = queue.take(); // waits safely
                        if (isWebSocketSession) {
                            WebSocketFrameEncoder.sendTextFrame(out, msg);
                        } else {
                            out.writeUTF(msg);
                            out.flush();
                        }
                    }
                } catch (Exception e) {
                    try { socket.close(); } catch (Exception ignored) {}
                }
            });
            writer.start();
        } catch (Exception e) {
            System.out.println("Cannot form a socket connection: " + e);
        }
    }

    public void send(String msg) {
        queue.offer(msg);
    }

    private void handleIncomingFile(String payload) {
        // Format: FILE_SEND|recipient|filename|base64Data
        String[] parts = payload.split("\\|", 4);
        if (parts.length >= 4) {
            String recipient = parts[1];
            String fileName = parts[2];
            String base64Data = parts[3];

            try {
                byte[] bytes = Base64.getDecoder().decode(base64Data);

                // Path target: storage/<recipient>/<fileName>
                String targetDirName = "storage/" + ("Broadcast (Everyone)".equals(recipient) ? "public" : recipient);
                File targetDir = new File(targetDirName);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                File destinationFile = new File(targetDir, fileName);
                Files.write(destinationFile.toPath(), bytes);
                System.out.println("[File Engine] Saved file from " + username + " to user storage: " + destinationFile.getAbsolutePath());

                String notifyMsg = "FILE_RECEIVED|" + (username != null ? username : "Anonymous") + "|" + fileName + "|" + destinationFile.getPath();

                if ("Broadcast (Everyone)".equals(recipient)) {
                    WebSocketSessionRegistry.broadcastRaw(notifyMsg);
                } else {
                    WebSocketSessionRegistry.sendDirectMessage(username != null ? username : "Anonymous", recipient, notifyMsg);
                }
            } catch (Exception e) {
                System.err.println("[File Engine Error] Could not save incoming file: " + e.getMessage());
            }
        }
    }

    public void run() {
        try {
            while (!socket.isClosed()) {
                if (isWebSocketSession) {
                    // Handle incoming RFC 6455 WebSocket Frames
                    WebSocketFrameParser.Frame frame = WebSocketFrameParser.parseFrame(in);
                    if (frame.getOpcode() == WebSocketFrameParser.Opcode.CLOSE) {
                        WebSocketFrameEncoder.sendCloseFrame(out);
                        WebSocketSessionRegistry.unregisterSession(username);
                        socket.close();
                        break;
                    } else if (frame.getOpcode() == WebSocketFrameParser.Opcode.TEXT) {
                        String msg = frame.getTextPayload();
                        
                        if (msg.startsWith("FILE_SEND|")) {
                            handleIncomingFile(msg);
                        } else if (msg.startsWith("CANVAS_CREATE|")) {
                            String[] parts = msg.split("\\|", 3);
                            if (parts.length >= 3) {
                                String roomId = parts[1];
                                String pin = parts[2];
                                String res = CanvasRoomManager.createRoom(roomId, pin, this);
                                send(res);
                            }
                        } else if (msg.startsWith("CANVAS_JOIN|")) {
                            String[] parts = msg.split("\\|", 3);
                            if (parts.length >= 3) {
                                String roomId = parts[1];
                                String pin = parts[2];
                                String res = CanvasRoomManager.joinRoom(roomId, pin, this);
                                send(res);
                            }
                        } else if (msg.startsWith("CANVAS_LEAVE|")) {
                            String[] parts = msg.split("\\|", 2);
                            if (parts.length >= 2) {
                                CanvasRoomManager.leaveRoom(parts[1], this);
                            }
                        } else if (msg.startsWith("CANVAS_DRAW|") || msg.startsWith("CANVAS_CLEAR|")) {
                            String[] parts = msg.split("\\|", 3);
                            if (parts.length >= 2) {
                                String roomId = parts[1];
                                CanvasRoomManager.broadcastToRoom(roomId, msg, this);
                            }
                        } else if (msg.startsWith("DRAW|") || msg.equals("DRAW_CLEAR")) {
                            WebSocketSessionRegistry.broadcastRaw(msg);
                        } else if (msg.startsWith("/pm ")) {
                            String[] parts = msg.split(" ", 3);
                            if (parts.length >= 3) {
                                String recipient = parts[1];
                                String pmText = parts[2];
                                boolean sent = WebSocketSessionRegistry.sendDirectMessage(username != null ? username : "Anonymous", recipient, pmText);
                                if (!sent) {
                                    send("[System]: User '" + recipient + "' is offline or not found.");
                                }
                            }
                        } else {
                            System.out.println("[WebSocket Rx] Message from " + (username != null ? username : "anonymous") + ": " + msg);
                            WebSocketSessionRegistry.broadcast(username != null ? username : "Anonymous", msg);
                        }
                    }
                } else {
                    // Handle standard HTTP or TCP Requests
                    String raw = in.readUTF();
                    System.out.println("[Server Rx] Received raw request: " + raw);

                    if (raw.startsWith("WS_UPGRADE|")) {
                        Request upgradeReq = RequestParser.parse(raw, this);
                        String userHandle = upgradeReq.params.get("user");
                        if (userHandle != null && !userHandle.isEmpty()) {
                            this.username = userHandle;
                        }
                        
                        // Send upgrade response FIRST over writeUTF before switching reader loop mode
                        out.writeUTF("WS_UPGRADE_SUCCESS");
                        out.flush();
                        
                        this.isWebSocketSession = true;
                        WebSocketSessionRegistry.registerSession(this.username, this);
                        System.out.println("[WebSocket Engine] Upgraded connection to RFC 6455 WebSocket session for user: " + this.username);
                        continue;
                    }

                    Request request = RequestParser.parse(raw, this);
                    String routeKey = request.method + "|" + request.path;
                    System.out.println("[Server Routing] Route key generated: " + routeKey);

                    Method method = Request_Mapping.getMethod(routeKey);

                    if (method != null) {
                        Object result = MethodInvoker.invoke(method, request);
                        if (result != null) {
                            System.out.println("[Server Tx] Sending response: " + result.toString());
                            send(result.toString());
                        }
                    } else {
                        System.err.println("[Server Routing Error] No method found for routeKey: " + routeKey);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Connection Thread] Client disconnected: " + e.getMessage());
            if (username != null) {
                WebSocketSessionRegistry.unregisterSession(username);
            }
        }
    }
}
