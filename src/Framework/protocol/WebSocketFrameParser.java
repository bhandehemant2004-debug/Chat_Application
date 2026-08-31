package Framework.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Pure Core Java RFC 6455 WebSocket Frame Parser
 */
public class WebSocketFrameParser {

    public enum Opcode {
        CONTINUATION(0x0),
        TEXT(0x1),
        BINARY(0x2),
        CLOSE(0x8),
        PING(0x9),
        PONG(0xA);

        private final int code;
        Opcode(int code) { this.code = code; }
        public int getCode() { return code; }

        public static Opcode fromCode(int code) {
            for (Opcode o : values()) {
                if (o.code == code) return o;
            }
            return TEXT;
        }
    }

    public static class Frame {
        private final boolean fin;
        private final Opcode opcode;
        private final String textPayload;
        private final byte[] rawPayload;

        public Frame(boolean fin, Opcode opcode, String textPayload, byte[] rawPayload) {
            this.fin = fin;
            this.opcode = opcode;
            this.textPayload = textPayload;
            this.rawPayload = rawPayload;
        }

        public boolean isFin() { return fin; }
        public Opcode getOpcode() { return opcode; }
        public String getTextPayload() { return textPayload; }
        public byte[] getRawPayload() { return rawPayload; }
    }

    /**
     * Reads and parses an incoming client WebSocket frame from DataInputStream according to RFC 6455
     */
    public static Frame parseFrame(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        boolean fin = (b1 & 0x80) != 0;
        int opcodeCode = b1 & 0x0F;
        Opcode opcode = Opcode.fromCode(opcodeCode);

        int b2 = in.readUnsignedByte();
        boolean masked = (b2 & 0x80) != 0;
        long payloadLength = b2 & 0x7F;

        if (payloadLength == 126) {
            payloadLength = in.readUnsignedShort();
        } else if (payloadLength == 127) {
            payloadLength = in.readLong();
        }

        byte[] maskingKey = new byte[4];
        if (masked) {
            in.readFully(maskingKey);
        }

        byte[] payload = new byte[(int) payloadLength];
        in.readFully(payload);

        // RFC 6455 Client Payload Unmasking Algorithm: unmasked[i] = masked[i] ^ maskKey[i % 4]
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
            }
        }

        String textPayload = new String(payload, StandardCharsets.UTF_8);
        return new Frame(fin, opcode, textPayload, payload);
    }
}
