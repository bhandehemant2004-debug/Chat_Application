package Framework.canvas;

import Thread.Reading_Writing;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CanvasRoomManager {

    public static class CanvasRoom {
        private String roomId;
        private String pin;
        private String creator;
        private Set<Reading_Writing> members = Collections.synchronizedSet(new HashSet<>());
        private List<String> strokeHistory = Collections.synchronizedList(new ArrayList<>());

        public CanvasRoom(String roomId, String pin, String creator) {
            this.roomId = roomId;
            this.pin = pin;
            this.creator = creator;
        }

        public boolean validatePin(String inputPin) {
            return this.pin.equals(inputPin);
        }

        public void addMember(Reading_Writing connection) {
            members.add(connection);
            // Replay existing stroke history to new joiner
            synchronized (strokeHistory) {
                for (String stroke : strokeHistory) {
                    connection.send(stroke);
                }
            }
        }

        public void removeMember(Reading_Writing connection) {
            members.remove(connection);
        }

        public void broadcast(String msg, Reading_Writing sender) {
            if (msg.startsWith("CANVAS_DRAW|")) {
                strokeHistory.add(msg);
            } else if (msg.startsWith("CANVAS_CLEAR|")) {
                strokeHistory.clear();
            }

            synchronized (members) {
                for (Reading_Writing member : members) {
                    if (member != sender && !member.socket.isClosed()) {
                        member.send(msg);
                    }
                }
            }
        }

        public int getMemberCount() {
            return members.size();
        }

        public String getRoomId() { return roomId; }
        public String getCreator() { return creator; }
    }

    private static final Map<String, CanvasRoom> activeRooms = new ConcurrentHashMap<>();

    public static synchronized String createRoom(String roomId, String pin, Reading_Writing creator) {
        if (activeRooms.containsKey(roomId)) {
            return "CANVAS_ERROR|Room already exists!";
        }
        CanvasRoom room = new CanvasRoom(roomId, pin, creator.username);
        room.addMember(creator);
        activeRooms.put(roomId, room);
        System.out.println("[CanvasRoomManager] Room created: " + roomId + " with PIN: " + pin + " by " + creator.username);
        return "CANVAS_JOIN_SUCCESS|" + roomId;
    }

    public static synchronized String joinRoom(String roomId, String pin, Reading_Writing user) {
        CanvasRoom room = activeRooms.get(roomId);
        if (room == null) {
            return "CANVAS_ERROR|Room '" + roomId + "' does not exist!";
        }
        if (!room.validatePin(pin)) {
            return "CANVAS_ERROR|Incorrect PIN for room '" + roomId + "'!";
        }
        room.addMember(user);
        System.out.println("[CanvasRoomManager] User " + user.username + " joined room: " + roomId);
        return "CANVAS_JOIN_SUCCESS|" + roomId;
    }

    public static void leaveRoom(String roomId, Reading_Writing user) {
        CanvasRoom room = activeRooms.get(roomId);
        if (room != null) {
            room.removeMember(user);
            System.out.println("[CanvasRoomManager] User " + user.username + " left room: " + roomId);
        }
    }

    public static void broadcastToRoom(String roomId, String msg, Reading_Writing sender) {
        CanvasRoom room = activeRooms.get(roomId);
        if (room != null) {
            room.broadcast(msg, sender);
        }
    }
}
