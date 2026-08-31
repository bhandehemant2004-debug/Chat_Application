package Framework.protocol;

import Security.IdentityManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Custom Secure Protocol Frame over TCP
 * Format:
 * [Magic Byte (1B = 0xAF)] [Type (1B)] [Payload Length (4B)] [RSA Signature (256B)] [Payload Data]
 */
public class SecureFrame {
    public static final byte MAGIC_BYTE = (byte) 0xAF;

    public enum FrameType {
        RAW_TEXT(0x01),
        AUTHENTICATE(0x02),
        CHAT_MESSAGE(0x03),
        SECURE_DISCONNECT(0x08);

        private final byte code;
        FrameType(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

        public static FrameType fromCode(byte code) {
            for (FrameType t : values()) {
                if (t.code == code) return t;
            }
            return RAW_TEXT;
        }
    }

    private final FrameType type;
    private final String payload;
    private final byte[] signature;

    public SecureFrame(FrameType type, String payload, byte[] signature) {
        this.type = type;
        this.payload = payload;
        this.signature = signature;
    }

    public FrameType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public byte[] getSignature() {
        return signature;
    }

    /**
     * Serializes frame to output stream
     */
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(MAGIC_BYTE);
        out.writeByte(type.getCode());
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        out.writeInt(payloadBytes.length);
        
        // Write RSA signature (256 bytes fixed length for 2048-bit RSA key)
        if (signature != null && signature.length == 256) {
            out.write(signature);
        } else {
            out.write(new byte[256]); // padding empty signature if none provided
        }
        
        out.write(payloadBytes);
        out.flush();
    }

    /**
     * Reads frame from incoming input stream
     */
    public static SecureFrame readFrom(DataInputStream in, String senderPublicKeyBase64, IdentityManager identityManager) throws Exception {
        byte magic = in.readByte();
        if (magic != MAGIC_BYTE) {
            throw new IllegalArgumentException("Invalid Protocol Magic Byte! Expected 0xAF but got: " + Integer.toHexString(magic & 0xFF));
        }

        byte typeByte = in.readByte();
        FrameType frameType = FrameType.fromCode(typeByte);

        int payloadLength = in.readInt();
        byte[] signature = new byte[256];
        in.readFully(signature);

        byte[] payloadBytes = new byte[payloadLength];
        in.readFully(payloadBytes);
        String payloadText = new String(payloadBytes, StandardCharsets.UTF_8);

        // Security check if RSA public key & identityManager are available
        if (senderPublicKeyBase64 != null && identityManager != null) {
            String signatureBase64 = Base64.getEncoder().encodeToString(signature);
            boolean isValid = identityManager.verifySignature(payloadText, signatureBase64, senderPublicKeyBase64);
            if (!isValid) {
                throw new SecurityException("SecureFrame RSA Signature Verification Failed!");
            }
        }

        return new SecureFrame(frameType, payloadText, signature);
    }
}
