
import com.naukma.network.messaging.MessageMapper;
import com.naukma.network.packet.*;
import com.naukma.network.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageMapperTest {

    private MessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MessageMapper();
    }

    @Test
    void shouldMapGetStockQuantity() {
        Message msg = new Message(1, 100, "P001".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(GetStockQuantityPacket.class, packet);
        GetStockQuantityPacket p = (GetStockQuantityPacket) packet;
        assertEquals("P001", p.productId());
    }

    @Test
    void shouldMapAddStock() {
        Message msg = new Message(2, 100, "P001:150".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(AddStockPacket.class, packet);
        AddStockPacket p = (AddStockPacket) packet;
        assertEquals("P001", p.productId());
        assertEquals(150, p.quantity());
    }

    @Test
    void shouldMapDeductStock() {
        Message msg = new Message(3, 100, "P001:30".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(DeductStockPacket.class, packet);
        DeductStockPacket p = (DeductStockPacket) packet;
        assertEquals("P001", p.productId());
        assertEquals(30, p.quantity());
    }

    @Test
    void shouldMapAddProductGroup() {
        Message msg = new Message(4, 100, "G001:Electronics".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(AddProductGroupPacket.class, packet);
        AddProductGroupPacket p = (AddProductGroupPacket) packet;
        assertEquals("G001", p.groupId());
        assertEquals("Electronics", p.groupName());
    }

    @Test
    void shouldMapAddProductToGroup() {
        Message msg = new Message(5, 100, "G001:P001".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(AddProductToGroupPacket.class, packet);
        AddProductToGroupPacket p = (AddProductToGroupPacket) packet;
        assertEquals("G001", p.groupId());
        assertEquals("P001", p.productId());
    }

    @Test
    void shouldMapSetProductPrice() {
        Message msg = new Message(6, 100, "P001:299.99".getBytes());
        Packet packet = mapper.toPacket(msg);

        assertInstanceOf(SetProductPricePacket.class, packet);
        SetProductPricePacket p = (SetProductPricePacket) packet;
        assertEquals("P001", p.productId());
        assertEquals(299.99, p.price(), 0.001);
    }

    @Test
    void shouldThrowOnUnknownMessageType() {
        Message msg = new Message(999, 100, "test".getBytes());
        assertThrows(RuntimeException.class, () -> mapper.toPacket(msg));
    }

    @Test
    void shouldThrowOnInvalidAddStockFormat() {
        Message msg = new Message(2, 100, "P001".getBytes());
        assertThrows(RuntimeException.class, () -> mapper.toPacket(msg));
    }

    @Test
    void shouldThrowOnInvalidSetPriceFormat() {
        Message msg = new Message(6, 100, "P001:abc".getBytes());
        assertThrows(RuntimeException.class, () -> mapper.toPacket(msg));
    }

    @Test
    void shouldThrowOnEmptyPayload() {
        Message msg = new Message(1, 100, new byte[0]);
        assertThrows(RuntimeException.class, () -> mapper.toPacket(msg));
    }

    @Test
    void shouldHandleNullPayloadGracefully() {
        Message msg = new Message(1, 100, null);
        assertThrows(RuntimeException.class, () -> mapper.toPacket(msg));
    }
}