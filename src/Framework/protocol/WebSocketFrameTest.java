package Framework.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * End-to-End Test Suite for RFC 6455 Frame Encoder & Parser
 */
public class WebSocketFrameTest {

    public static void main(String[] args) throws Exception {
        System.out.println("====== [WebSocket RFC 6455 Framing Test] ======");

        String originalMessage = "Hello Core Java WebSockets!";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // 1. Encode frame using WebSocketFrameEncoder
        WebSocketFrameEncoder.sendTextFrame(dos, originalMessage);
        byte[] frameBytes = baos.toByteArray();
        System.out.println("Encoded Frame Byte Count: " + frameBytes.length);

        // 2. Decode frame using WebSocketFrameParser
        ByteArrayInputStream bais = new ByteArrayInputStream(frameBytes);
        DataInputStream dis = new DataInputStream(bais);

        WebSocketFrameParser.Frame decodedFrame = WebSocketFrameParser.parseFrame(dis);

        System.out.println("Decoded Opcode: " + decodedFrame.getOpcode());
        System.out.println("Decoded FIN Bit: " + decodedFrame.isFin());
        System.out.println("Decoded Payload Text: " + decodedFrame.getTextPayload());

        boolean matches = originalMessage.equals(decodedFrame.getTextPayload());
        System.out.println("Payload Match Verification: " + matches);
        if (!matches) {
            throw new RuntimeException("Payload mismatch during WebSocket framing!");
        }

        System.out.println("====== [WebSocket Frame Test Passed Successfully] ======");
    }
}
