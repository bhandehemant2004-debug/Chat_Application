package Framework.session;

import Thread.Reading_Writing;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSocketSessionRegistry {

    private static final Map<String, Reading_Writing> activeSessions = new ConcurrentHashMap<>();

    public static void registerSession(String username, Reading_Writing connection) {
        if (username != null && !username.trim().isEmpty()) {
            activeSessions.put(username, connection);
            System.out.println("[SessionRegistry] User registered to WebSocket session: " + username + " (Total Active: " + activeSessions.size() + ")");
            broadcastActiveUsers();
        }
    }

    public static void unregisterSession(String username) {
        if (username != null) {
            activeSessions.remove(username);
            System.out.println("[SessionRegistry] User disconnected: " + username + " (Remaining Active: " + activeSessions.size() + ")");
            broadcastActiveUsers();
        }
    }

    public static void broadcast(String sender, String message) {
        System.out.println("[SessionRegistry] Broadcasting live message from " + sender + " to " + activeSessions.size() + " connected client(s).");
        String formattedMsg = "[" + sender + "]: " + message;
        for (Map.Entry<String, Reading_Writing> entry : activeSessions.entrySet()) {
            try {
                entry.getValue().send(formattedMsg);
            } catch (Exception e) {
                System.err.println("[SessionRegistry] Failed to queue message to user " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    public static void broadcastRaw(String rawMessage) {
        for (Map.Entry<String, Reading_Writing> entry : activeSessions.entrySet()) {
            try {
                entry.getValue().send(rawMessage);
            } catch (Exception ignored) {}
        }
    }

    public static boolean sendDirectMessage(String sender, String recipient, String message) {
        Reading_Writing recipientSession = activeSessions.get(recipient);
        if (recipientSession != null) {
            try {
                String pm = "[PM from " + sender + "]: " + message;
                recipientSession.send(pm);
                
                // Echo back to sender
                Reading_Writing senderSession = activeSessions.get(sender);
                if (senderSession != null && !sender.equals(recipient)) {
                    senderSession.send("[PM to " + recipient + "]: " + message);
                }
                return true;
            } catch (Exception e) {
                System.err.println("[SessionRegistry] Direct message error: " + e.getMessage());
            }
        }
        return false;
    }

    public static void broadcastActiveUsers() {
        String userList = activeSessions.keySet().stream().collect(Collectors.joining(","));
        String updateMsg = "USER_LIST_UPDATE|" + userList;
        System.out.println("[SessionRegistry] Broadcasting user list update: " + updateMsg);
        for (Map.Entry<String, Reading_Writing> entry : activeSessions.entrySet()) {
            try {
                entry.getValue().send(updateMsg);
            } catch (Exception ignored) {}
        }
    }

    public static Map<String, Reading_Writing> getActiveSessions() {
        return activeSessions;
    }
}
