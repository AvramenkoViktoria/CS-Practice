import com.naukma.network.messaging.FakeReceiver;
import com.naukma.network.messaging.RawMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageReceiverTest {

    private FakeReceiver receiver;

    @BeforeEach
    void setUp() {
        receiver = new FakeReceiver();
    }

    @Test
    void shouldReturnValidRawMessage() {
        RawMessage raw = receiver.receive();

        assertNotNull(raw);
        assertNotNull(raw.data());
        assertTrue(raw.data().length > 20);
    }

    @Test
    void shouldGenerateDifferentPacketTypes() {
        for (int i = 0; i < 20; i++) {
            RawMessage raw = receiver.receive();
            assertNotNull(raw);
        }
    }
}
