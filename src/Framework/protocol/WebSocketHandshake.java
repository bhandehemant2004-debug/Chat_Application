package Framework.protocol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Pure Java implementation of WebSocket Handshake (RFC 6455)
 */
public class WebSocketHandshake {

    private static final String WEBSOCKET_MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static String calculateAcceptKey(String clientKey) {
        try {
            String concatenated = clientKey.trim() + WEBSOCKET_MAGIC_GUID;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha1.digest(concatenated.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate Sec-WebSocket-Accept header", e);
        }
    }

    public static String buildResponseHeader(String acceptKey) {
        return "HTTP/1.1 101 Switching Protocols\r\n" +
               "Upgrade: websocket\r\n" +
               "Connection: Upgrade\r\n" +
               "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
    }

    public static void main(String[] args) {
        String sampleClientKey = "dGhlIHNhbXBsZSBub25jZQ==";
        String acceptKey = calculateAcceptKey(sampleClientKey);
        System.out.println("[WebSocket Handshake RFC 6455 Test]");
        System.out.println("Client Key: " + sampleClientKey);
        System.out.println("Computed Sec-WebSocket-Accept Key: " + acceptKey);
        System.out.println("Expected RFC 6455 Output: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=");
        System.out.println("Match: " + "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=".equals(acceptKey));
    }
}
