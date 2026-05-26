import com.naukma.network.encryption.Crc16;
import com.naukma.network.encryption.PacketEncoder;
import com.naukma.network.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PacketEncoderTest {

    private PacketEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new PacketEncoder();
    }

    @Test
    void shouldEncodeAndContainCorrectStructure() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] packet = encoder.encode(message, (byte) 1, 100L);

        assertNotNull(packet);
        assertTrue(packet.length >= 16 + 2, "Packet must contain header + at least message CRC");

        assertEquals((byte) 0x13, packet[0], "First byte should be magic byte 0x13");
        assertEquals(1, packet[1], "Second byte should be source");
    }

    @Test
    void shouldProduceDifferentPacketsForDifferentIds() throws Exception {
        Message msg = new Message(1, 100, "TEST".getBytes());
        byte[] p1 = encoder.encode(msg, (byte) 1, 1L);
        byte[] p2 = encoder.encode(msg, (byte) 1, 2L);

        assertNotEquals(p1, p2, "Different packetId should produce different packets");
    }

    @Test
    void shouldHaveCorrectHeaderLength() throws Exception {
        Message message = new Message(2, 100, "P001:150".getBytes());
        byte[] packet = encoder.encode(message, (byte) 5, 123456789L);

        assertEquals(0x13, packet[0] & 0xFF);
        assertEquals(5, packet[1] & 0xFF);           // src
    }

    @Test
    void shouldCalculateHeaderCrcCorrectly() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] packet = encoder.encode(message, (byte) 1, 100L);

        byte[] headerForCrc = Arrays.copyOfRange(packet, 0, 14);
        short expectedCrc = Crc16.calculate(headerForCrc);
        short actualCrc = ByteBuffer.wrap(packet, 14, 2).getShort();

        assertEquals(expectedCrc, actualCrc, "Header CRC is incorrect");
    }

    @Test
    void shouldCalculateMessageCrcCorrectly() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] packet = encoder.encode(message, (byte) 1, 100L);

        int encryptedLength = ByteBuffer.wrap(packet, 10, 4).getInt();
        byte[] encryptedData = Arrays.copyOfRange(packet, 16, 16 + encryptedLength);
        short expectedCrc = Crc16.calculate(encryptedData);
        short actualCrc = ByteBuffer.wrap(packet, 16 + encryptedLength, 2).getShort();

        assertEquals(expectedCrc, actualCrc, "Message CRC is incorrect");
    }

    @Test
    void shouldEncodeDifferentMessageTypes() throws Exception {
        int[] types = {1, 2, 3, 4, 5, 6};

        for (int type : types) {
            Message msg = new Message(type, 100, "P001:100".getBytes());
            byte[] packet = encoder.encode(msg, (byte) 1, (long) type * 1000);

            assertNotNull(packet);
            assertTrue(packet.length > 20);
        }
    }

    @Test
    void shouldEncryptData() throws Exception {
        String originalPayload = "VerySecretPayload123";
        Message original = new Message(1, 100, originalPayload.getBytes());

        byte[] packet = encoder.encode(original, (byte) 1, 42L);

        int encryptedLength = ByteBuffer.wrap(packet, 10, 4).getInt();
        byte[] encryptedPart = Arrays.copyOfRange(packet, 16, 16 + encryptedLength);

        assertFalse(Arrays.equals(originalPayload.getBytes(), encryptedPart),
                "Data should be encrypted and not appear in plaintext");
    }

    @Test
    void shouldProduceSamePacketForSameInput() throws Exception {
        Message msg = new Message(1, 100, "P001".getBytes());
        byte[] packet1 = encoder.encode(msg, (byte) 1, 9999L);
        byte[] packet2 = encoder.encode(msg, (byte) 1, 9999L);

        assertArrayEquals(packet1, packet2);
    }
}