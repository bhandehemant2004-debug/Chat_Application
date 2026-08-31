package Framework.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Pure Core Java RFC 6455 WebSocket Frame Encoder for Server-to-Client frame transmission
 */
public class WebSocketFrameEncoder {

    /**
     * Encodes a text message into an unmasked RFC 6455 binary frame and writes to output stream
     */
    public static void sendTextFrame(DataOutputStream out, String message) throws IOException {
        byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
        int length = payloadBytes.length;

        // 0x81 -> FIN bit (1) + Text Opcode (0x1)
        out.writeByte(0x81);

        // Server-to-Client frames are NOT masked in RFC 6455
        if (length <= 125) {
            out.writeByte(length);
        } else if (length <= 65535) {
            out.writeByte(126);
            out.writeShort(length);
        } else {
            out.writeByte(127);
            out.writeLong(length);
        }

        out.write(payloadBytes);
        out.flush();
    }

    /**
     * Sends a Close connection frame (Opcode 0x8)
     */
    public static void sendCloseFrame(DataOutputStream out) throws IOException {
        out.writeByte(0x88); // FIN + Close
        out.writeByte(0);
        out.flush();
    }
}
