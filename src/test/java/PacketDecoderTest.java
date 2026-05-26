
import com.naukma.network.encryption.PacketDecoder;
import com.naukma.network.encryption.PacketEncoder;
import com.naukma.network.messaging.Message;
import com.naukma.network.messaging.MessageMapper;
import com.naukma.network.packet.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacketDecoderTest {

    private PacketDecoder decoder;
    private PacketEncoder encoder;
    private MessageMapper mapper;

    @BeforeEach
    void setUp() {
        decoder = new PacketDecoder();
        encoder = new PacketEncoder();
        mapper = new MessageMapper();
    }

    @Test
    void shouldDecodeValidPacket() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] encoded = encoder.encode(message, (byte) 1, 12345L);

        Message decodedMessage = decoder.decode(encoded);

        assertEquals(1, decodedMessage.getType());
        assertEquals(100, decodedMessage.getUserId());
        assertArrayEquals("P001".getBytes(), decodedMessage.getPayload());
    }

    @Test
    void shouldDecodeAndMapToCorrectPacket() throws Exception {
        Message message = new Message(2, 100, "P001:250".getBytes());
        byte[] encoded = encoder.encode(message, (byte) 1, 54321L);

        Message decodedMessage = decoder.decode(encoded);
        Packet packet = mapper.toPacket(decodedMessage);

        assertInstanceOf(AddStockPacket.class, packet);
        AddStockPacket addPacket = (AddStockPacket) packet;
        assertEquals("P001", addPacket.productId());
        assertEquals(250, addPacket.quantity());
    }

    @Test
    void shouldThrowExceptionOnInvalidMagicByte() {
        assertThrows(Exception.class, () -> {
            byte[] invalid = new byte[]{0x00, 0x01, 0x02};
            decoder.decode(invalid);
        });
    }

    @Test
    void shouldThrowExceptionOnInvalidHeaderCRC() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] encoded = encoder.encode(message, (byte) 1, 100L);

        encoded[14] = (byte) ~encoded[14];
        encoded[15] = (byte) ~encoded[15];

        assertThrows(Exception.class, () -> decoder.decode(encoded),
                "Should throw on invalid header CRC");
    }

    @Test
    void shouldThrowExceptionOnInvalidMessageCRC() throws Exception {
        Message message = new Message(1, 100, "P001".getBytes());
        byte[] encoded = encoder.encode(message, (byte) 1, 100L);

        int lastIndex = encoded.length - 1;
        encoded[lastIndex] = (byte) ~encoded[lastIndex];
        encoded[lastIndex - 1] = (byte) ~encoded[lastIndex - 1];

        assertThrows(Exception.class, () -> decoder.decode(encoded),
                "Should throw on invalid message CRC");
    }

    @Test
    void shouldThrowOnTooShortPacket() {
        assertThrows(Exception.class, () -> {
            byte[] tooShort = new byte[10];
            decoder.decode(tooShort);
        });
    }

    @Test
    void shouldThrowOnEmptyPacket() {
        assertThrows(Exception.class, () -> decoder.decode(new byte[0]));
    }

    @Test
    void shouldDecodeDifferentPacketTypes() throws Exception {
        Object[][] testCases = {
                {1, "P001", GetStockQuantityPacket.class},
                {2, "P001:100", AddStockPacket.class},
                {3, "P001:50", DeductStockPacket.class},
                {4, "G001:Electronics", AddProductGroupPacket.class},
                {5, "G001:P001", AddProductToGroupPacket.class},
                {6, "P001:499.99", SetProductPricePacket.class}
        };

        for (Object[] testCase : testCases) {
            int type = (int) testCase[0];
            String payload = (String) testCase[1];
            Class<?> expectedClass = (Class<?>) testCase[2];

            Message msg = new Message(type, 100, payload.getBytes());
            byte[] encoded = encoder.encode(msg, (byte) 1, 10000L + type);

            Message decoded = decoder.decode(encoded);
            Packet packet = mapper.toPacket(decoded);

            assertInstanceOf(expectedClass, packet);
        }
    }

    @Test
    void shouldPreservePacketIdAndSource() throws Exception {
        long expectedPacketId = 987654321L;
        byte expectedSrc = 5;

        Message message = new Message(1, 100, "TEST".getBytes());
        byte[] encoded = encoder.encode(message, expectedSrc, expectedPacketId);

        Message decoded = decoder.decode(encoded);

        assertNotNull(decoded);
    }
}